package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysDictDataConvert;
import com.etl.api.domain.entity.SysDictData;
import com.etl.api.domain.form.SysDictDataCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.SysDictDataMapper;
import com.etl.api.service.SysDictDataService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 字典数据表 服务层实现。
 *
 * @author chen
 * @since 2026-05-12
 */
@Service
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Override
    public ResponseVO<Void> addDictData(SysDictDataCreateForm form) {
        val typeId = form.getTypeId();
        val label = form.getLabel();

        val exists = this.queryChain()
                .eq(SysDictData::getTypeId, typeId)
                .eq(SysDictData::getLabel, label)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(typeId + "-" + label);
        }

        val entity = SysDictDataConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }
}
