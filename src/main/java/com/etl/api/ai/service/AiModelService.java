package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI模型配置表 服务层。
 *
 * @author chen
 * @since 2026-11-11
 */
public interface AiModelService extends IService<AiModel> {

    ResponseVO<Void> addAiModel(AiModelCreateForm form);

    ResponseVO<Void> updateAiModel(AiModelUpdateForm form);
}
