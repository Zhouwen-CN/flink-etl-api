package com.etl.api.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.InetAddressException;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.XdbException;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * <pre>
 * 三种缓存策略：
 *   - NoCache：文件查询
 *   - VIndexCache：向量索引
 *   - BufferCache：全内存
 * 注意：
 *   1. setCacheSliceBytes：大文件分片缓存，解决oom文件，仅适用于 BufferCache
 *   2. setSearchers：查询器个数，仅适用于 NoCache / VIndexCache
 *   3. NoCache / VIndexCache策略，需要指定文件路径，不能从classpath加载文件
 *      资源文件被嵌套在JAR包内，不再是文件系统上的一个独立文件，ResourceUtils.getFile()会失败。
 * </pre>
 */
@Slf4j
public final class IP2RegionUtil {

    private static final Ip2Region ip2Region;

    static {
        val resource = new ClassPathResource("db/ip2region/ip2region_v4.xdb");
        try {
            // 创建 v4 的配置：指定缓存策略和 v4 的 xdb 文件路径
            val v4Config = Config.custom()
                    .setCachePolicy(Config.BufferCache)               // 指定缓存策略:  NoCache / VIndexCache / BufferCache
                    .setCacheSliceBytes(Searcher.DEFAULT_SLICE_BYTES) // 设置缓存的分片字节数，默认为 50MiB
                    .setXdbInputStream(resource.getInputStream())     // 设置 v4 xdb 文件的 inputstream 对象
                    // .setCachePolicy(Config.VIndexCache)
                    // .setSearchers(SEARCHERS)                         // 设置初始化的查询器数量
                    // .setXdbFile(resource.getFile())                  // 设置 v4 xdb File 对象
                    .asV4();

            ip2Region = Ip2Region.create(v4Config, null);
        } catch (IOException | XdbException | InvalidConfigException e) {
            throw new RuntimeException("创建 ip2region 对象失败", e);
        }
    }

    private IP2RegionUtil() {

    }

    public static String search(String ipv4String) {
        try {
            return ip2Region.search(ipv4String);
        } catch (InetAddressException | IOException | InterruptedException e) {
            log.error("ip2region 查询失败: {}", e.getMessage());
            return null;
        }
    }
}
