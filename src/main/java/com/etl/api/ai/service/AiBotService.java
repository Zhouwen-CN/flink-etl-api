package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI Bot表 服务层。
 *
 * @author chen
 * @since 2026-11-13
 */
public interface AiBotService extends IService<AiBot> {

    ResponseVO<Void> addAiBot(AiBotCreateForm form);

    ResponseVO<Void> updateAiBot(AiBotUpdateForm form);
}
