package com.etl.api.service.impl;

import com.etl.api.domain.convert.DictDataConvert;
import com.etl.api.domain.entity.DictData;
import com.etl.api.domain.form.DictDataCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.DictDataMapper;
import com.etl.api.service.DictDataService;
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
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    @Override
    public ResponseVO<Void> addDictData(DictDataCreateForm form) {
        val typeId = form.getTypeId();
        val label = form.getLabel();

        val exists = this.queryChain()
                .eq(DictData::getTypeId, typeId)
                .eq(DictData::getLabel, label)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(typeId + "-" + label);
        }

        val entity = DictDataConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }
}
