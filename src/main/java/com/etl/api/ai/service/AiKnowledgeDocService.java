package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiKnowledgeDoc;
import com.etl.api.ai.domain.vo.AiKnowledgeDocVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI 知识库文档表 服务层。
 *
 * @author chen
 * @since 2026-11-20
 */
public interface AiKnowledgeDocService extends IService<AiKnowledgeDoc> {

    /**
     * 上传文档：扩展名/大小校验 → blob 入库（WAITING）→ 提交异步解析，返回即成功
     */
    ResponseVO<Void> addDoc(Long knowledgeId, MultipartFile file);

    /**
     * 更新文档（重新上传）：校验 → 清旧向量/分块 → 替换 blob 与文件信息（WAITING）→ 提交异步解析
     */
    ResponseVO<Void> updateDoc(Long documentId, MultipartFile file);

    /**
     * 文档分页（含 chunk_count、parse_status，不带 blob 大字段）
     */
    PageVO<AiKnowledgeDocVO> getPage(Long knowledgeId, Integer currentPage, Integer pageSize, String searchFileName);

    /**
     * 下载原始文件（单独查询 file_content 大字段，返回文件流）
     */
    AiKnowledgeDoc getDocForDownload(Long documentId);

    /**
     * 重解析：blob 在库，删旧向量+旧分块 → 按知识库当前分块参数重走流水线
     */
    ResponseVO<Void> reparse(Long documentId);

    /**
     * 删除文档（级联：向量库数据 → 分块表 → 文档表）
     */
    ResponseVO<Void> removeDoc(Long documentId);
}
