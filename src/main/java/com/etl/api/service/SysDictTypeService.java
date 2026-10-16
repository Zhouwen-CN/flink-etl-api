package com.etl.api.service;

import com.etl.api.domain.entity.SysDictType;
import com.etl.api.domain.form.SysDictTypeCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 字典类型表 服务层。
 *
 * @author chen
 * @since 2026-05-12
 */
public interface SysDictTypeService extends IService<SysDictType> {

    ResponseVO<Void> addDictType(SysDictTypeCreateForm form);

    ResponseVO<Void> removeDictType(Long id);

    ResponseVO<Void> removeBatchDictType(Collection<Long> ids);
}
