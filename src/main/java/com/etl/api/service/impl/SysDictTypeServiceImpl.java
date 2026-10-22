package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysDictTypeConvert;
import com.etl.api.domain.entity.SysDictData;
import com.etl.api.domain.entity.SysDictType;
import com.etl.api.domain.form.SysDictTypeCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.SysDictDataMapper;
import com.etl.api.mapper.SysDictTypeMapper;
import com.etl.api.service.SysDictTypeService;
import com.mybatisflex.core.query.QueryWrapper;
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
    private final SysDictDataMapper dictDataMapper;

    @Override
    public ResponseVO<Void> addDictType(SysDictTypeCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(SysDictType::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = SysDictTypeConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDictType(Long id) {
        val count = dictDataMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(SysDictData::getTypeId, id)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeBatchDictType(Collection<Long> ids) {
        val count = dictDataMapper.selectCountByQuery(
                QueryWrapper.create()
                        .in(SysDictData::getTypeId, ids)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有字典数据依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
