package com.etl.api.ai.service.impl;

import com.etl.api.ai.config.AiVectorStoreFactory;
import com.etl.api.ai.domain.convert.AiVectorDatabaseConvert;
import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.domain.form.AiVectorDatabaseCreateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseUpdateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseVerifyForm;
import com.etl.api.ai.domain.vo.AiVectorDatabaseVO;
import com.etl.api.ai.mapper.AiVectorDatabaseMapper;
import com.etl.api.ai.service.AiVectorDatabaseService;
import com.etl.api.sys.domain.vo.PageVO;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * AI 向量数据库表 服务层实现。
 *
 * @author chen
 * @since 2026-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVectorDatabaseServiceImpl extends ServiceImpl<AiVectorDatabaseMapper, AiVectorDatabase> implements AiVectorDatabaseService {

    private final AiVectorStoreFactory vectorStoreFactory;

    @Override
    public PageVO<AiVectorDatabaseVO> getPage(Integer currentPage, Integer pageSize, String searchName) {
        val page = this.queryChain()
                .like(AiVectorDatabase::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AiVectorDatabaseVO.class);
        return PageVO.from(page);
    }

    @Override
    public ResponseVO<Void> addVectorDatabase(AiVectorDatabaseCreateForm form) {
        val exists = this.queryChain()
                .eq(AiVectorDatabase::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }

        // 连接验证（含 type 白名单 + config_json 本地校验 + 只读探测）：失败整体保存失败，保证落库即连通
        vectorStoreFactory.verifyConnection(form.getType(), form.getConfigJson());

        val entity = AiVectorDatabaseConvert.INSTANCE.convert(form);
        this.save(entity);
        log.info("新增向量数据库, id: {}, name: {}, type: {}", entity.getId(), entity.getName(), entity.getType());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateVectorDatabase(AiVectorDatabaseUpdateForm form) {
        val old = this.getById(form.getId());
        if (old == null) {
            return ResponseVO.recordNotFoundError(form.getId());
        }

        val nameExists = this.queryChain()
                .eq(AiVectorDatabase::getName, form.getName())
                .ne(AiVectorDatabase::getId, form.getId())
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError(form.getName());
        }

        // 前端每次修改都会回传旧值：type 或 config_json 变更时重新连接验证，失败整体更新失败保持旧值
        val typeChanged = !Objects.equals(form.getType(), old.getType());
        val configChanged = !Objects.equals(form.getConfigJson(), old.getConfigJson());
        if (typeChanged || configChanged) {
            vectorStoreFactory.verifyConnection(form.getType(), form.getConfigJson());
        }

        val entity = AiVectorDatabaseConvert.INSTANCE.convert(form);
        // 未变更时保持原值（避免规范化后的文本覆盖原始配置）
        entity.setType(typeChanged ? form.getType() : old.getType());
        entity.setConfigJson(configChanged ? form.getConfigJson() : old.getConfigJson());

        this.updateById(entity);
        if (typeChanged || configChanged) {
            vectorStoreFactory.evictByConfigId(entity.getId());
        }
        log.info("更新向量数据库, id: {}, name: {}, typeChanged: {}, configChanged: {}",
                entity.getId(), entity.getName(), typeChanged, configChanged);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeVectorDatabase(Long id) {
        vectorStoreFactory.evictByConfigId(id);
        this.removeById(id);
        // 后续知识库模块挂载此库时需补级联引用检查
        log.info("删除向量数据库, id: {}", id);
        return ResponseVO.ok();
    }

    @Override
    public void verifyConnection(AiVectorDatabaseVerifyForm form) {
        // 异常（本地校验失败 / 连接失败）上抛全局处理器返回具体原因
        vectorStoreFactory.verifyConnection(form.getType(), form.getConfigJson());
    }
}
