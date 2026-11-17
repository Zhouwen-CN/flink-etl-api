package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * AI Bot表 服务层。
 *
 * @author chen
 * @since 2026-11-13
 */
public interface AiBotService extends IService<AiBot> {

    ResponseVO<Void> addAiBot(AiBotCreateForm form);

    ResponseVO<Void> updateAiBot(AiBotUpdateForm form);

    /**
     * 查询bot挂载的工具ID列表（前端数据回显）
     */
    List<Long> getToolIds(Long botId);
}
