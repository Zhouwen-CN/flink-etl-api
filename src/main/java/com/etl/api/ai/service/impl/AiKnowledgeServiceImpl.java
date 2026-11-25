package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiKnowledgeConvert;
import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.entity.AiKnowledgeDoc;
import com.etl.api.ai.domain.form.AiKnowledgeCreateForm;
import com.etl.api.ai.domain.form.AiKnowledgeUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeVO;
import com.etl.api.ai.mapper.AiKnowledgeMapper;
import com.etl.api.ai.service.AiKnowledgeDocService;
import com.etl.api.ai.service.AiKnowledgeService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 知识库表 服务层实现。
 *
 * <p>Embedding 模型、向量集合与检索参数均由 yaml 全局配置（AiEmbeddingConfig / AiVectorDbConfig /
 * AiRetrievalProperties），知识库仅保留名称/描述与分块参数；创建时不主动连向量库建集合——
 * LangChain4j Milvus store 在首次使用时自动建集合，惰性即可。
 *
 * @author chen
 * @since 2026-11-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiKnowledgeServiceImpl extends ServiceImpl<AiKnowledgeMapper, AiKnowledge> implements AiKnowledgeService {

    private final AiKnowledgeDocService docService;

    @Override
    public PageVO<AiKnowledgeVO> getPage(Integer currentPage, Integer pageSize, String searchName) {
        val page = this.queryChain()
                .like(AiKnowledge::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AiKnowledgeVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> addKnowledge(AiKnowledgeCreateForm form) {
        val nameExists = this.queryChain()
                .eq(AiKnowledge::getName, form.getName())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        val chunkParamsError = validateChunkParams(form.getChunkSize(), form.getOverlapSize());
        if (chunkParamsError != null) {
            return ResponseVO.error(chunkParamsError);
        }

        val entity = AiKnowledgeConvert.INSTANCE.convert(form);
        this.save(entity);
        log.info("新增知识库, id: {}, name: {}", entity.getId(), entity.getName());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateKnowledge(AiKnowledgeUpdateForm form) {
        val old = this.getById(form.getId());
        if (old == null) {
            return ResponseVO.recordNotFoundError("ID", form.getId());
        }

        val nameExists = this.queryChain()
                .eq(AiKnowledge::getName, form.getName())
                .ne(AiKnowledge::getId, form.getId())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError("Name", form.getName());
        }

        val chunkParamsError = validateChunkParams(form.getChunkSize(), form.getOverlapSize());
        if (chunkParamsError != null) {
            return ResponseVO.error(chunkParamsError);
        }

        val entity = AiKnowledgeConvert.INSTANCE.convert(form);
        this.updateById(entity);
        log.info("更新知识库, id: {}, name: {}", entity.getId(), entity.getName());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeKnowledge(Long id) {
        // 知识库下仍有文档时不允许删除，需先清空全部文档
        val hasDocs = docService.queryChain()
                .eq(AiKnowledgeDoc::getKnowledgeId, id)
                .exists();
        if (hasDocs) {
            return ResponseVO.error("删除失败，尚有文档依赖");
        }
        this.removeById(id);
        log.info("删除知识库, id: {}", id);
        return ResponseVO.ok();
    }

    /**
     * 分块参数约束校验（可空走 DDL 默认值；同时提供时须 overlap < chunk）
     */
    private String validateChunkParams(Integer chunkSize, Integer overlapSize) {
        if (chunkSize != null && overlapSize != null && overlapSize >= chunkSize) {
            return "分块参数非法: overlapSize(" + overlapSize + ") 必须小于 chunkSize(" + chunkSize + ")";
        }
        return null;
    }
}
