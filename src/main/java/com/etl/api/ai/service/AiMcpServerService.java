package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.form.AiMcpServerCreateForm;
import com.etl.api.ai.domain.form.AiMcpServerUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpSelectorVO;
import com.etl.api.ai.domain.vo.AiMcpServerVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * AI MCP服务表 服务层。
 *
 * @author chen
 * @since 2026-11-17
 */
public interface AiMcpServerService extends IService<AiMcpServer> {

    /**
     * 分页查询（config_json 脱敏返回）
     */
    PageVO<AiMcpServerVO> getPage(Integer currentPage, Integer pageSize, String searchName);

    /**
     * 新增（校验 config_json + 同步工具，同步失败整体失败）
     */
    ResponseVO<Void> addAiMcpServer(AiMcpServerCreateForm form);

    /**
     * 更新（config_json 变更时同步工具，同步失败整体失败保持旧值）
     */
    ResponseVO<Void> updateAiMcpServer(AiMcpServerUpdateForm form);

    /**
     * 删除（物理删除该服务的工具缓存并级联清理挂载行）
     */
    ResponseVO<Void> removeAiMcpServer(Long id);

    /**
     * 手动触发工具同步（失败仅返回错误原因，不影响已有数据）
     */
    ResponseVO<Void> syncTools(Long id);

    /**
     * MCP选择器（树形：server节点 -> tool叶子，供bot挂载工具的 el-tree-select 渲染）
     */
    List<AiMcpSelectorVO> selector();
}
