package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.entity.AiKnowledgeDoc;
import com.etl.api.ai.domain.entity.AiKnowledgeDocChunk;
import com.etl.api.ai.domain.vo.AiKnowledgeDocVO;
import com.etl.api.ai.enumeration.AiDocParseStatusEnum;
import com.etl.api.ai.mapper.AiKnowledgeDocChunkMapper;
import com.etl.api.ai.mapper.AiKnowledgeDocMapper;
import com.etl.api.ai.service.AiKnowledgeDocService;
import com.etl.api.ai.service.manager.AiVectorStoreManager;
import com.etl.api.ai.util.FileTypeDetectUtil;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * AI 知识库文档表 服务层实现。
 *
 * <p>上传即异步解析（提交后返回，前端轮询 parse_status）；file_content 为 isLarge 大字段，
 * 常规查询不加载，下载/重解析时显式 select。删除/重解析级联：向量库数据 → 分块 → 文档。
 *
 * @author chen
 * @since 2026-11-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeDocServiceImpl extends ServiceImpl<AiKnowledgeDocMapper, AiKnowledgeDoc>
        implements AiKnowledgeDocService {

    /**
     * 上传文件大小上限（50MB）
     */
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;

    private final AiKnowledgeDocChunkMapper chunkMapper;
    private final AiKnowledgeDocIngestService ingestService;
    private final AiVectorStoreManager vectorStoreManager;

    @Override
    public ResponseVO<Void> addDoc(Long knowledgeId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseVO.error("请上传文件");
        }
        val originalFilename = file.getOriginalFilename();
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseVO.error("文件过大，最大支持 50MB");
        }

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (Exception e) {
            return ResponseVO.error("读取文件内容失败: " + e.getMessage());
        }

        // 基于 Tika 内容探测（魔数）校验真实类型，防止伪造扩展名
        val fileType = FileTypeDetectUtil.detectFileType(fileContent, originalFilename);
        if (fileType == null) {
            return ResponseVO.error("不支持的文件类型或文件内容与扩展名不符，仅支持: " +
                    String.join("/", FileTypeDetectUtil.SUPPORTED_FILE_TYPES));
        }

        val doc = AiKnowledgeDoc.builder()
                .knowledgeId(knowledgeId)
                .fileName(originalFilename)
                .fileType(fileType)
                .fileContent(fileContent)
                .parseStatus(AiDocParseStatusEnum.WAITING.getCode())
                .build();
        this.save(doc);
        log.info("上传文档, id: {}, knowledgeId: {}, fileName: {}, size: {}",
                doc.getId(), knowledgeId, originalFilename, file.getSize());

        ingestService.submitParse(doc.getId());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateDoc(Long documentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseVO.error("请上传文件");
        }
        val doc = this.getById(documentId);
        if (doc == null) {
            return ResponseVO.recordNotFoundError("DocumentId", documentId);
        }
        if (AiDocParseStatusEnum.PROCESSING.getCode().equals(doc.getParseStatus())) {
            return ResponseVO.error("文档解析中，请稍后再试");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ResponseVO.error("文件过大，最大支持 50MB");
        }

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (Exception e) {
            return ResponseVO.error("读取文件内容失败: " + e.getMessage());
        }

        // 基于 Tika 内容探测（魔数）校验真实类型，防止伪造扩展名
        val originalFilename = file.getOriginalFilename();
        val fileType = FileTypeDetectUtil.detectFileType(fileContent, originalFilename);
        if (fileType == null) {
            return ResponseVO.error("不支持的文件类型或文件内容与扩展名不符，仅支持: " +
                    String.join("/", FileTypeDetectUtil.SUPPORTED_FILE_TYPES));
        }

        // 校验全部通过后才清旧数据：删旧向量+旧分块（新文件即将重走解析）
        vectorStoreManager.removeByDocument(doc.getKnowledgeId(), documentId);

        // UpdateChain 显式 set null，清掉旧全文与失败原因（updateById 默认忽略 null 字段）
        UpdateChain.of(AiKnowledgeDoc.class)
                .set(AiKnowledgeDoc::getFileName, originalFilename)
                .set(AiKnowledgeDoc::getFileType, fileType)
                .set(AiKnowledgeDoc::getFileContent, fileContent)
                .set(AiKnowledgeDoc::getContent, null)
                .set(AiKnowledgeDoc::getParseStatus, AiDocParseStatusEnum.WAITING.getCode())
                .set(AiKnowledgeDoc::getParseFailReason, null)
                .eq(AiKnowledgeDoc::getId, documentId)
                .update();

        log.info("更新文档, id: {}, knowledgeId: {}, fileName: {}, size: {}",
                documentId, doc.getKnowledgeId(), originalFilename, file.getSize());
        ingestService.submitParse(documentId);
        return ResponseVO.ok();
    }

    @Override
    public PageVO<AiKnowledgeDocVO> getPage(Long knowledgeId, Integer currentPage, Integer pageSize,
                                            String searchFileName) {
        val page = this.queryChain()
                .eq(AiKnowledgeDoc::getKnowledgeId, knowledgeId, Objects::nonNull)
                .like(AiKnowledgeDoc::getFileName, searchFileName, StringUtils.hasText(searchFileName))
                .pageAs(Page.of(currentPage, pageSize), AiKnowledgeDocVO.class);
        // chunk_count 统计（页内 ≤ pageSize 条，逐条 count 简单可靠）
        page.getRecords().forEach(vo ->
                vo.setChunkCount(chunkMapper.selectCountByQuery(QueryWrapper.create()
                        .eq(AiKnowledgeDocChunk::getDocumentId, vo.getId()))));
        return PageVO.from(page);
    }

    @Override
    public AiKnowledgeDoc getDocForDownload(Long documentId) {
        return this.queryChain()
                .select(AiKnowledgeDoc::getFileName, AiKnowledgeDoc::getFileType, AiKnowledgeDoc::getFileContent)
                .eq(AiKnowledgeDoc::getId, documentId)
                .one();
    }

    @Override
    public ResponseVO<Void> reparse(Long documentId) {
        // 常规查询不带 blob（重解析由异步流水线自行加载）
        val doc = this.getById(documentId);
        if (doc == null) {
            return ResponseVO.recordNotFoundError("DocumentId", documentId);
        }

        // 清空该文档的向量与分块后重走流水线（分块参数取知识库当前配置）
        vectorStoreManager.removeByDocument(doc.getKnowledgeId(), documentId);

        val update = AiKnowledgeDoc.builder()
                .id(documentId)
                .parseStatus(AiDocParseStatusEnum.WAITING.getCode())
                .build();
        this.updateById(update);

        ingestService.submitParse(documentId);

        log.info("文档重新解析已提交, documentId: {}", documentId);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDoc(Long documentId) {
        val doc = this.getById(documentId);
        if (doc == null) {
            return ResponseVO.recordNotFoundError("DocumentId", documentId);
        }

        // 先删向量库与分块，再删文档本身
        vectorStoreManager.removeByDocument(doc.getKnowledgeId(), documentId);

        this.removeById(documentId);
        log.info("删除文档, id: {}, knowledgeId: {}", documentId, doc.getKnowledgeId());
        return ResponseVO.ok();
    }
}
