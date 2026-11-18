package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelSelectorForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * AI模型配置表 服务层。
 *
 * @author chen
 * @since 2026-11-11
 */
public interface AiModelService extends IService<AiModel> {

    ResponseVO<Void> addAiModel(AiModelCreateForm form);

    ResponseVO<Void> updateAiModel(AiModelUpdateForm form);

    /**
     * 模型选择器：拉取 baseUrl 下可用模型列表
     */
    ResponseVO<List<String>> modelSelector(AiModelSelectorForm form);

    /**
     * 按模型类型测试连通性：Chat 对话、Embedding 向量化、Rerank 重排
     */
    ResponseVO<String> testModel(Long modelId);
}
