package com.etl.api.ai.util;

import org.apache.tika.Tika;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 文件类型校验工具：基于 Tika 内容探测（魔数）验证真实类型，防止伪造扩展名。
 *
 * <p>pdf/docx/xlsx 有文件头魔数特征，按内容判定并信任内容类型；
 * md/txt 无魔数特征（内容探测均为 text/plain），回退扩展名白名单校验。
 *
 * @author chen
 * @since 2026-11-20
 */
public final class FileTypeDetectUtil {

    /**
     * 支持的文件扩展名白名单
     */
    public static final Set<String> SUPPORTED_FILE_TYPES = Set.of("pdf", "docx", "xlsx", "md", "txt");

    /**
     * 二进制格式：内容探测 MIME → 扩展名
     */
    private static final Map<String, String> MIME_TO_EXT = Map.of(
            "application/pdf", "pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");


    /**
     * file_type → 下载 Content-Type 映射
     */
    public static final Map<String, String> CONTENT_TYPES = Map.of(
            "pdf", "application/pdf",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "md", "text/plain;charset=UTF-8",
            "txt", "text/plain;charset=UTF-8"
    );

    /**
     * 内容为文本时可接受的扩展名
     */
    private static final Set<String> TEXT_FILE_TYPES = Set.of("md", "txt");

    private static final Tika TIKA = new Tika();

    private FileTypeDetectUtil() {
    }

    /**
     * 从文件名提取扩展名并校验白名单
     *
     * @param originalFilename 原始文件名
     * @return 小写扩展名（如 pdf），不支持类型返回 null
     */
    public static String resolveFileType(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return null;
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1)
                .toLowerCase(Locale.ROOT);
        return SUPPORTED_FILE_TYPES.contains(ext) ? ext : null;
    }

    /**
     * 基于 Tika 内容探测验证真实文件类型，防止伪造扩展名
     *
     * <p>pdf/docx/xlsx 按文件头魔数判定，信任内容（扩展名伪造会被纠正或拒绝）；
     * md/txt 无魔数特征，先确认内容为文本，再回退扩展名判定。
     *
     * @param fileContent      文件字节
     * @param originalFilename 原始文件名
     * @return 小写扩展名，内容与白名单不符返回 null
     */
    public static String detectFileType(byte[] fileContent, String originalFilename) {
        String ext = resolveFileType(originalFilename);
        if (ext == null || fileContent == null || fileContent.length == 0) {
            return null;
        }
        String mime = TIKA.detect(fileContent);
        // 二进制格式：魔数判定，信任内容类型
        String contentExt = MIME_TO_EXT.get(mime);
        if (contentExt != null) {
            return contentExt;
        }
        // 文本格式：md/txt 无魔数特征，内容均为 text/plain，回退扩展名
        if (mime.startsWith("text/")) {
            return TEXT_FILE_TYPES.contains(ext) ? ext : null;
        }
        return null;
    }
}
