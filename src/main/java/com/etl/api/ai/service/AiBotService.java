package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.service.tool.AiServiceTool;

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

    /**
     * 构建 bot 挂载的 MCP 工具集合（聊天时提供给大模型）。
     *
     * <p>ToolSpecification 由缓存 input_schema 反序列化得到；执行器临时建连调用远端工具，
     * 结果按工具的 output_template 包装后回传模型。
     *
     * @return 工具为空时返回空 List
     */
    List<AiServiceTool> buildChatTools(Long botId);
}
