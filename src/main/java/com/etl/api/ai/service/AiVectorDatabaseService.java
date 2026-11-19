package com.etl.api.ai.service;

import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.domain.form.AiVectorDatabaseCreateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseUpdateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseVerifyForm;
import com.etl.api.ai.domain.vo.AiVectorDatabaseVO;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * AI 向量数据库表 服务层。
 *
 * @author chen
 * @since 2026-11-18
 */
public interface AiVectorDatabaseService extends IService<AiVectorDatabase> {

    /**
     * 分页查询
     */
    PageVO<AiVectorDatabaseVO> getPage(Integer currentPage, Integer pageSize, String searchName);

    /**
     * 新增（连接验证失败则整体保存失败，保证落库即连通）
     */
    ResponseVO<Void> addVectorDatabase(AiVectorDatabaseCreateForm form);

    /**
     * 更新（type 或 config_json 变更时连接验证，失败整体更新失败保持旧值 + 失效旧缓存实例）
     */
    ResponseVO<Void> updateVectorDatabase(AiVectorDatabaseUpdateForm form);

    /**
     * 删除（失效缓存实例 + 物理删除；后续知识库模块挂载此库时需补级联引用检查）
     */
    ResponseVO<Void> removeVectorDatabase(Long id);

    /**
     * 连接验证（支持未保存配置，失败原因经全局异常处理器返回）
     */
    void verifyConnection(AiVectorDatabaseVerifyForm form);
}
