package com.etl.api.ai.config;

import com.etl.api.ai.domain.dto.AiVectorDbConfig;
import com.etl.api.ai.domain.dto.AiVectorDbMilvusConfigDTO;
import com.etl.api.ai.domain.dto.AiVectorDbRedisConfigDTO;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.domain.validator.AiVectorDbConfigValidator;
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
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.UnifiedJedis;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 向量数据库客户端工厂（与 ChatModelFactory 并列）。
 *
 * <p>按 config 类型（instanceof）分发构建 {@link EmbeddingStore}，当前支持 Redis（Redis Stack）与 Milvus。
 * 缓存分两层，连接数从 O(集合数) 降到 O(配置数)：
 * <ul>
 *   <li>连接层：{@code configId|sha256(configJson)} -> 底层连接（UnifiedJedis / MilvusServiceClient），
 *       同一配置的全部集合共享一条连接（线程安全：Jedis 连接池 / Milvus gRPC channel）；</li>
 *   <li>Store 层：{@code 连接key|knowledgeId} -> EmbeddingStore，仅轻量包装，不含连接资源。</li>
 * </ul>
 * configJson 摘要参与 key，改配置后新调用自然落到新 key（多实例部署无需失效广播）；
 * 并发场景下 per-key 双检锁防止重复建连。
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
     * 集合名基础前缀，各 create 方法按自身命名约束拼接知识库 id（Knowledge Base）
     */
    private static final String COLLECTION_NAME_PREFIX = "kb";


    private final AiVectorDbConfigValidator configValidator;

    /**
     * 连接层缓存：configId|sha256(configJson) -> 共享连接（两个字段按 type 二选一非空）
     */
    private final ConcurrentHashMap<String, SharedConnection> connections = new ConcurrentHashMap<>();

    /**
     * Store 层缓存：连接key|knowledgeId -> EmbeddingStore（轻量包装，不含连接资源）
     */
    private final ConcurrentHashMap<String, EmbeddingStore<TextSegment>> stores = new ConcurrentHashMap<>();

    /**
     * per-key 锁：两层 key 结构不相交（连接层 2 段 / Store 层 3 段），共用一个锁表
     */
    private final ConcurrentHashMap<String, ReentrantLock> keyLocks = new ConcurrentHashMap<>();

    /**
     * 获取 EmbeddingStore（带两层缓存复用）。
     * 集合名由 create 方法按知识库 id 自行派生（Redis kb:{id} / Milvus kb_{id}，命名约束不同），
     * dimension 创建时探测固化，均不可变且由知识库侧保证合法，此处不再逐次校验。
     *
     * @param knowledge      知识库实体（提供派生集合名的 id 与固化维度）
     * @param vectorDatabase 向量数据库配置实体
     * @param metadataKeys   需要持久化/过滤的 segment metadata key（Redis 侧必须显式声明；
     *                       Milvus 动态字段自动存储，仅参与 Store 缓存 key），可为 null
     */
    public EmbeddingStore<TextSegment> getEmbeddingStore(AiKnowledge knowledge,
                                                         AiVectorDatabase vectorDatabase,
                                                         List<String> metadataKeys) {
        if (knowledge == null || vectorDatabase == null) {
            throw new AiVectorDbConfigException("知识库或向量数据库配置不能为空");
        }

        // 本地校验（type 白名单 + config_json 结构），失败即抛 AiVectorDbConfigException
        val vectorDbConfig = configValidator.validate(vectorDatabase.getType(), vectorDatabase.getConfigJson());
        // 连接层缓存key：configId|sha256(configJson)
        val connKey = vectorDatabase.getId() + "|" + sha256Hex(vectorDatabase.getConfigJson());
        // 维度
        val dimension = knowledge.getDimension();

        // 第一层：共享连接（配置不变时，同一配置的全部集合复用同一条连接）
        val connection = getOrCreateConnection(connKey, vectorDbConfig);

        // 第二层：Store 包装（Redis 创建近乎零成本；Milvus 首次可能触发 collection RPC）
        return getOrCreateStore(connKey, vectorDbConfig, connection, knowledge.getId(), dimension, metadataKeys);
    }

    /**
     * 获取或创建 Store 层包装（双检锁，镜像 {@link #getOrCreateConnection}）。
     * Store 缓存 key 直接用知识库 id + metadataKeys（集合名是其纯函数）；集合名由 create 方法按类型派生。
     * 持锁期间校验连接未被并发 evict（配置更新/删除），防止缓存到引用已关闭连接的 Store。
     */
    private EmbeddingStore<TextSegment> getOrCreateStore(String connKey, AiVectorDbConfig vectorDbConfig,
                                                         SharedConnection connection,
                                                         Long knowledgeId, Integer dimension,
                                                         List<String> metadataKeys) {
        val storeKey = connKey + "|" + knowledgeId + "|" + (metadataKeys == null ? "" : String.join(",", metadataKeys));
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
            EmbeddingStore<TextSegment> created;
            if (vectorDbConfig instanceof AiVectorDbRedisConfigDTO) {
                created = createRedisStore(connection.jedis(), knowledgeId, dimension, metadataKeys);
            } else if (vectorDbConfig instanceof AiVectorDbMilvusConfigDTO) {
                created = createMilvusStore(connection.milvusClient(), knowledgeId, dimension);
            } else {
                throw new AiVectorDbConfigException("不支持的向量数据库配置类型: " + vectorDbConfig.getClass().getSimpleName());
            }
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
        val config = configValidator.validate(type, configJson);
        if (config instanceof AiVectorDbRedisConfigDTO redis) {
            verifyRedis(redis);
        } else if (config instanceof AiVectorDbMilvusConfigDTO milvus) {
            verifyMilvus(milvus);
        } else {
            throw new AiVectorDbConfigException("不支持的向量数据库配置类型: " + config.getClass().getSimpleName());
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

    /**
     * 获取或创建连接层共享连接（双检锁；创建失败不缓存，下次调用重试）
     */
    private SharedConnection getOrCreateConnection(String connKey, AiVectorDbConfig config) {
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
            SharedConnection created;
            if (config instanceof AiVectorDbRedisConfigDTO redis) {
                created = new SharedConnection(createUnifiedJedis(redis), null);
            } else if (config instanceof AiVectorDbMilvusConfigDTO milvus) {
                created = new SharedConnection(null, createMilvusClient(milvus));
            } else {
                throw new AiVectorDbConfigException("不支持的向量数据库配置类型: " + config.getClass().getSimpleName());
            }
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
     * 构建 Redis EmbeddingStore（轻量包装共享连接，创建近乎零成本）。
     * 索引名按知识库 id 派生 kb:{id}（Redis 侧 ':' 合法）；prefix 必须显式跟随索引名——
     * SDK 默认 prefix 是常量 "embedding:"，不设置会导致所有集合的数据 key 共享同一前缀、
     * 索引互相覆盖彼此的数据（跨知识库召回）。
     */
    private EmbeddingStore<TextSegment> createRedisStore(UnifiedJedis jedis, Long knowledgeId, Integer dimension,
                                                         List<String> metadataKeys) {
        try {
            val builder = RedisEmbeddingStore.builder()
                    .unifiedJedis(jedis)
                    .indexName(COLLECTION_NAME_PREFIX + "_" + knowledgeId)
                    .prefix(COLLECTION_NAME_PREFIX + "_" + knowledgeId + ":")
                    .dimension(dimension);
            if (metadataKeys != null && !metadataKeys.isEmpty()) {
                builder.metadataKeys(metadataKeys);
            }
            return builder.build();
        } catch (Exception e) {
            // 共享连接保持缓存（仍然有效），仅本次 Store 创建失败
            throw new AiVectorDbConfigException("Redis 向量库创建失败: " + e.getMessage());
        }
    }

    /**
     * 构建 RedisClient（host+port，带 5s 超时；连接池惰性建连）。
     * RedisClient 为 UnifiedJedis 子类，RedisEmbeddingStore 仍按 UnifiedJedis 接收。
     */
    private UnifiedJedis createUnifiedJedis(AiVectorDbRedisConfigDTO config) {
        return RedisClient.builder()
                .hostAndPort(new HostAndPort(config.getHost(), config.getPort()))
                .clientConfig(
                        DefaultJedisClientConfig.builder()
                                .connectionTimeoutMillis(REDIS_TIMEOUT_MILLIS)
                                .socketTimeoutMillis(REDIS_TIMEOUT_MILLIS)
                                .user(config.getUsername())
                                .password(config.getPassword())
                                .build()
                )
                .build();
    }

    private void verifyRedis(AiVectorDbRedisConfigDTO config) {
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
     * collection 名按知识库 id 派生 kb_{id}（Milvus 命名仅字母/数字/下划线且不能以数字开头，':' 非法）。
     * MilvusEmbeddingStore 自身不可关闭，连接生命周期由连接层统一管理。
     */
    private EmbeddingStore<TextSegment> createMilvusStore(MilvusServiceClient client, Long knowledgeId, Integer dimension) {
        try {
            return MilvusEmbeddingStore.builder()
                    .milvusClient(client)
                    .collectionName(COLLECTION_NAME_PREFIX + "_" + knowledgeId)
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
    private MilvusServiceClient createMilvusClient(AiVectorDbMilvusConfigDTO config) {
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
    private void verifyMilvus(AiVectorDbMilvusConfigDTO config) {
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
