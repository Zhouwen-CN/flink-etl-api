package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.form.AiKnowledgeCreateForm;
import com.etl.api.ai.domain.form.AiKnowledgeUpdateForm;
import com.etl.api.ai.domain.vo.AiKnowledgeVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI 知识库表 服务层。
 *
 * @author chen
 * @since 2026-11-19
 */
public interface AiKnowledgeService extends IService<AiKnowledge> {

    /**
     * 分页查询
     */
    PageVO<AiKnowledgeVO> getPage(Integer currentPage, Integer pageSize, String searchName);

    /**
     * 新增（含 Embedding 维度探测，探测失败整体保存失败，保证落库配置可用）
     */
    ResponseVO<Void> addKnowledge(AiKnowledgeCreateForm form);

    /**
     * 更新（绑定类字段 vectorDbId/embedModelId/dimension 不可变更；集合名按 id 派生）
     */
    ResponseVO<Void> updateKnowledge(AiKnowledgeUpdateForm form);

    /**
     * 删除（物理删除；知识库下仍有文档时拒绝，需先清空全部文档）
     */
    ResponseVO<Void> removeKnowledge(Long id);
}
