package com.etl.api.ai.config;

import com.etl.api.ai.domain.dto.MilvusConfigDTO;
import com.etl.api.ai.domain.dto.RedisConfigDTO;
import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.domain.validator.AiVectorDbConfigValidator;
import com.etl.api.ai.enumeration.AiVectorDbTypeEnum;
import com.etl.api.ai.exception.AiVectorDbConfigException;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.HasCollectionParam;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.UnifiedJedis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * 向量数据库客户端工厂（与 ChatModelFactory 并列）。
 *
 * <p>按 type 分发构建 {@link EmbeddingStore}，当前支持 Redis（Redis Stack）与 Milvus。
 * 缓存分两层，连接数从 O(集合数) 降到 O(配置数)：
 * <ul>
 *   <li>连接层：{@code configId|sha256(configJson)} -> 底层连接（UnifiedJedis / MilvusServiceClient），
 *       同一配置的全部集合共享一条连接（线程安全：Jedis 连接池 / Milvus gRPC channel）；</li>
 *   <li>Store 层：{@code 连接key|collectionName|dimension} -> EmbeddingStore，仅轻量包装，不含连接资源。</li>
 * </ul>
 * configJson 摘要参与 key，改配置后新调用自然落到新 key（多实例部署无需失效广播）；
 * dimension 参与 Store key，避免同库不同维度冲突。并发场景下 per-key 双检锁防止重复建连。
 * 配置更新/删除后由调用方触发 {@link #evictByConfigId(Long)} 失效两层缓存并关闭连接。
 *
 * <p>注意：SDK 调用收敛在本工厂内，后续 LC4j / Milvus 升级只改本类。
 *
 * @author chen
 * @since 2026-11-18
 */
@Component
@RequiredArgsConstructor
public class AiVectorStoreFactory {

    /**
     * Redis 连接/操作超时（毫秒），verify 与 store 复用
     */
    private static final int REDIS_TIMEOUT_MILLIS = 5000;

    /**
     * Milvus 连接验证 / 关闭等待时间（秒）
     */
    private static final long MILVUS_CLOSE_TIMEOUT_SECONDS = 5;

    /**
     * collectionName 白名单（Redis index / Milvus collection 命名约束）
     */
    private static final Pattern COLLECTION_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    private final AiVectorDbConfigValidator configValidator;

    /**
     * 连接层缓存：configId|sha256(configJson) -> 共享连接（两个字段按 type 二选一非空）
     */
    private final ConcurrentHashMap<String, SharedConnection> connections = new ConcurrentHashMap<>();

    /**
     * Store 层缓存：连接key|collectionName|dimension -> EmbeddingStore（轻量包装，不含连接资源）
     */
    private final ConcurrentHashMap<String, EmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();

    /**
     * per-key 锁：两层 key 结构不相交（连接层 2 段 / Store 层 4 段），共用一个锁表
     */
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    /**
     * 获取 EmbeddingStore（带两层缓存复用）
     *
     * @param entity         向量数据库配置实体
     * @param collectionName 集合名（Redis index / Milvus collection，仅字母/数字/下划线）
     * @param dimension      向量维度（来自知识库绑定的 embedding 模型，首建后固化）
     */
    public EmbeddingStore<TextSegment> getEmbeddingStore(AiVectorDatabase entity,
                                                         String collectionName, Integer dimension) {
        if (entity == null) {
            throw new AiVectorDbConfigException("向量数据库配置不能为空");
        }
        if (dimension == null || dimension <= 0) {
            throw new AiVectorDbConfigException("向量维度 dimension 必须为正整数");
        }
        if (!StringUtils.hasText(collectionName) || !COLLECTION_NAME_PATTERN.matcher(collectionName).matches()) {
            throw new AiVectorDbConfigException("collectionName 仅允许字母、数字、下划线: " + collectionName);
        }

        // 本地校验（type 白名单 + config_json 结构），失败即抛 AiVectorDbConfigException
        val config = configValidator.validate(entity.getType(), entity.getConfigJson());
        val dbType = requireSupportedType(entity.getType());
        val connKey = connectionKey(entity.getId(), entity.getConfigJson());

        // 第一层：共享连接（配置不变时，同一配置的全部集合复用同一条连接）
        val connection = getOrCreateConnection(connKey, dbType, config);

        // 第二层：Store 包装（Redis 创建近乎零成本；Milvus 首次可能触发 collection RPC）
        val storeKey = connKey + "|" + collectionName + "|" + dimension;
        val existing = stores.get(storeKey);
        if (existing != null) {
            return existing;
        }

        val lock = keyLocks.computeIfAbsent(storeKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双检：持锁期间可能已被并发线程创建
            val cached = stores.get(storeKey);
            if (cached != null) {
                return cached;
            }
            // 连接若已被并发 evict（配置更新/删除），中止本次创建，防止缓存到引用已关闭连接的 Store
            if (connections.get(connKey) != connection) {
                throw new AiVectorDbConfigException("向量数据库配置正在变更，请重试");
            }
            val created = switch (dbType) {
                case REDIS -> createRedisStore(connection.jedis(), collectionName, dimension);
                case MILVUS -> createMilvusStore(connection.milvusClient(), collectionName, dimension);
            };
            stores.put(storeKey, created);
            return created;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 连接验证（只读探测，无写副作用，不需要 dimension）。
     * Redis 通过 ftList() 同时验证连通性与 RediSearch 模块在位
     * （RedisEmbeddingStore 硬依赖 Redis Stack，普通 Redis 在此暴露）；
     * Milvus 通过连接 + hasCollection 只读探测（返回即连通）。
     *
     * @throws AiVectorDbConfigException 本地校验失败或连接验证失败（含具体原因）
     */
    public void verifyConnection(Integer type, String configJson) {
        val dbType = requireSupportedType(type);
        val config = configValidator.validate(type, configJson);
        switch (dbType) {
            case REDIS -> verifyRedis((RedisConfigDTO) config);
            case MILVUS -> verifyMilvus((MilvusConfigDTO) config);
        }
    }

    /**
     * 按配置 id 失效两层缓存（更新/删除配置后调用）：先清 Store 层，再关闭连接层底层连接。
     * 后续知识库模块挂载此库时的级联引用检查由知识库侧补充。
     */
    public void evictByConfigId(Long configId) {
        if (configId == null) {
            return;
        }
        val prefix = configId + "|";
        stores.keySet().removeIf(key -> key.startsWith(prefix));
        connections.forEach((key, connection) -> {
            if (key.startsWith(prefix)) {
                connections.remove(key);
                connection.close();
            }
        });
    }

    private AiVectorDbTypeEnum requireSupportedType(Integer type) {
        return AiVectorDbTypeEnum.of(type)
                .orElseThrow(() -> new AiVectorDbConfigException(
                        "不支持的向量数据库类型: " + type + "，仅允许: 1-redis、2-milvus"));
    }

    /**
     * 连接层缓存key：configId|sha256(configJson)
     * configJson 摘要进 key 保证改配置后新调用自然落到新 key
     */
    private String connectionKey(Long configId, String configJson) {
        return configId + "|" + sha256Hex(configJson);
    }

    /**
     * 获取或创建连接层共享连接（双检锁；创建失败不缓存，下次调用重试）
     */
    private SharedConnection getOrCreateConnection(String connKey, AiVectorDbTypeEnum dbType, Object config) {
        val existing = connections.get(connKey);
        if (existing != null) {
            return existing;
        }

        val lock = keyLocks.computeIfAbsent(connKey, k -> new ReentrantLock());
        lock.lock();
        try {
            // 双检：持锁期间可能已被并发线程创建
            val cached = connections.get(connKey);
            if (cached != null) {
                return cached;
            }
            val created = switch (dbType) {
                case REDIS -> new SharedConnection(createUnifiedJedis((RedisConfigDTO) config), null);
                case MILVUS -> new SharedConnection(null, createMilvusClient((MilvusConfigDTO) config));
            };
            connections.put(connKey, created);
            return created;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 连接层缓存条目：底层连接 + 统一关闭入口（两个字段按 type 二选一非空）
     */
    private record SharedConnection(UnifiedJedis jedis, MilvusServiceClient milvusClient) {

        void close() {
            if (jedis != null) {
                runQuietly(jedis);
            }
            if (milvusClient != null) {
                closeMilvusQuietly(milvusClient);
            }
        }
    }

    // ---------------- Redis ----------------

    /**
     * 构建 Redis EmbeddingStore（轻量包装共享连接，创建近乎零成本）
     */
    private EmbeddingStore<TextSegment> createRedisStore(UnifiedJedis jedis, String collectionName, Integer dimension) {
        try {
            return RedisEmbeddingStore.builder()
                    .unifiedJedis(jedis)
                    .indexName(collectionName)
                    .dimension(dimension)
                    .build();
        } catch (Exception e) {
            // 共享连接保持缓存（仍然有效），仅本次 Store 创建失败
            throw new AiVectorDbConfigException("Redis 向量库创建失败: " + e.getMessage());
        }
    }

    /**
     * 构建 UnifiedJedis（host+port，带 5s 超时；连接池惰性建连）
     */
    private UnifiedJedis createUnifiedJedis(RedisConfigDTO config) {
        val builder = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(REDIS_TIMEOUT_MILLIS)
                .socketTimeoutMillis(REDIS_TIMEOUT_MILLIS);
        if (StringUtils.hasText(config.getUsername())) {
            builder.user(config.getUsername().trim());
        }
        if (StringUtils.hasText(config.getPassword())) {
            builder.password(config.getPassword().trim());
        }
        return new UnifiedJedis(new HostAndPort(config.getHost().trim(), config.getPort()), builder.build());
    }

    private void verifyRedis(RedisConfigDTO config) {
        try (UnifiedJedis jedis = createUnifiedJedis(config)) {
            jedis.ftList();
        } catch (AiVectorDbConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new AiVectorDbConfigException("Redis 连接验证失败: " + e.getMessage());
        }
    }

    // ---------------- Milvus ----------------

    /**
     * 构建 Milvus EmbeddingStore（包装共享 gRPC channel）。
     * MilvusEmbeddingStore 自身不可关闭，连接生命周期由连接层统一管理。
     */
    private EmbeddingStore<TextSegment> createMilvusStore(MilvusServiceClient client, String collectionName, Integer dimension) {
        try {
            return MilvusEmbeddingStore.builder()
                    .milvusClient(client)
                    .collectionName(collectionName)
                    .dimension(dimension)
                    .metricType(MetricType.COSINE)
                    .build();
        } catch (Exception e) {
            // 共享连接保持缓存（仍然有效），仅本次 Store 创建失败
            throw new AiVectorDbConfigException("Milvus 向量库创建失败: " + e.getMessage());
        }
    }

    /**
     * 构建 MilvusServiceClient（host+port，鉴权可选，databaseName 可选）
     */
    private MilvusServiceClient createMilvusClient(MilvusConfigDTO config) {
        val builder = ConnectParam.newBuilder()
                .withHost(config.getHost().trim())
                .withPort(config.getPort());
        if (StringUtils.hasText(config.getUsername()) && StringUtils.hasText(config.getPassword())) {
            builder.withAuthorization(config.getUsername().trim(), config.getPassword().trim());
        }
        if (StringUtils.hasText(config.getDatabaseName())) {
            builder.withDatabaseName(config.getDatabaseName().trim());
        }
        return new MilvusServiceClient(builder.build());
    }

    /**
     * Milvus 连接验证：连接 + hasCollection 只读探测（Success 即连通），finally close
     */
    private void verifyMilvus(MilvusConfigDTO config) {
        MilvusServiceClient client = null;
        try {
            client = createMilvusClient(config);
            val resp = client.hasCollection(HasCollectionParam.newBuilder()
                    .withCollectionName("__vectordb_verify__")
                    .build());
            if (resp.getStatus() != R.Status.Success.getCode()) {
                throw new AiVectorDbConfigException("Milvus 连接验证失败: " + resp.getMessage());
            }
        } catch (AiVectorDbConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new AiVectorDbConfigException("Milvus 连接验证失败: " + e.getMessage());
        } finally {
            closeMilvusQuietly(client);
        }
    }

    // ---------------- 公共 ----------------

    private static void closeMilvusQuietly(MilvusServiceClient client) {
        try {
            if (client != null) {
                client.close(MILVUS_CLOSE_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
            // 防御性关闭失败不影响主流程
        }
    }

    private static void runQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ignored) {
            // 防御性关闭失败不影响主流程
        }
    }

    private String sha256Hex(String input) {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            val bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            val sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}
