package com.etl.api.ai.service.impl;

import com.etl.api.ai.domain.convert.AiBotConvert;
import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import com.etl.api.ai.mapper.AiBotMapper;
import com.etl.api.ai.service.AiBotService;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * AI Bot表 服务层实现。
 *
 * @author chen
 * @since 2026-11-13
 */
@Service
@RequiredArgsConstructor
public class AiBotServiceImpl extends ServiceImpl<AiBotMapper, AiBot> implements AiBotService {

    @Override
    public ResponseVO<Void> addAiBot(AiBotCreateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        this.save(AiBotConvert.INSTANCE.convert(form));
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> updateAiBot(AiBotUpdateForm form) {
        val exists = this.queryChain()
                .eq(AiBot::getName, form.getName())
                .ne(AiBot::getId, form.getId())
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(form.getName());
        }
        // 不忽略null
        this.updateById(AiBotConvert.INSTANCE.convert(form), false);
        return ResponseVO.ok();
    }
}
