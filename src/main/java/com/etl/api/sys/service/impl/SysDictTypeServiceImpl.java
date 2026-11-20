package com.etl.api.sys.service.impl;

import com.etl.api.sys.domain.convert.SysDictTypeConvert;
import com.etl.api.sys.domain.entity.SysDictData;
import com.etl.api.sys.domain.entity.SysDictType;
import com.etl.api.sys.domain.form.SysDictTypeCreateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.etl.api.sys.mapper.SysDictTypeMapper;
import com.etl.api.sys.service.SysDictTypeService;
import com.mybatisflex.core.query.QueryChain;
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
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public ResponseVO<Void> addDictType(SysDictTypeCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(SysDictType::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError("Name", name);
        }

        val entity = SysDictTypeConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDictType(Long id) {
        val exists = QueryChain.of(SysDictData.class)
                .eq(SysDictData::getTypeId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeBatchDictType(Collection<Long> ids) {
        val exists = QueryChain.of(SysDictData.class)
                .in(SysDictData::getTypeId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
