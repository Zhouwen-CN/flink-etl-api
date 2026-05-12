package com.etl.api.service.impl;

import com.etl.api.domain.convert.DictTypeConvert;
import com.etl.api.domain.entity.DictData;
import com.etl.api.domain.entity.DictType;
import com.etl.api.domain.form.DictTypeCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.DictTypeMapper;
import com.etl.api.service.DictDataService;
import com.etl.api.service.DictTypeService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 字典类型表 服务层实现。
 *
 * @author chen
 * @since 2026-05-12
 */
@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {
    private final DictDataService dictDataService;

    @Override
    public ResponseVO<Void> addDictType(DictTypeCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(DictType::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = DictTypeConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDictType(Long id) {
        val exists = dictDataService.queryChain()
                .eq(DictData::getTypeId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeBatchDictType(Collection<Long> ids) {
        val exists = dictDataService.queryChain()
                .in(DictData::getTypeId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
