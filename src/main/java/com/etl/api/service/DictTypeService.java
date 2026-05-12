package com.etl.api.service;

import com.etl.api.domain.entity.DictType;
import com.etl.api.domain.form.DictTypeCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 字典类型表 服务层。
 *
 * @author chen
 * @since 2026-05-12
 */
public interface DictTypeService extends IService<DictType> {

    ResponseVO<Void> addDictType(DictTypeCreateForm form);

    ResponseVO<Void> removeDictType(Long id);

    ResponseVO<Void> removeBatchDictType(Collection<Long> ids);
}
