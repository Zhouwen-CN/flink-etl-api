package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiModelConvert;
import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.ai.mapper.AiModelMapper;
import com.etl.api.ai.service.AiModelService;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * AI模型配置表 服务层实现。
 *
 * @author chen
 * @since 2026-11-11
 */
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel> implements AiModelService {

    @Override
    public ResponseVO<Void> addAiModel(AiModelCreateForm form) {
        val exists = this.queryChain()
                .eq(AiModel::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        this.save(AiModelConvert.INSTANCE.convert(form));
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiModel(AiModelUpdateForm form) {
        val exists = this.queryChain()
                .eq(AiModel::getName, form.getName())
                .ne(AiModel::getId, form.getId())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        this.updateById(AiModelConvert.INSTANCE.convert(form));
        return ResponseVO.ok();
    }
}
