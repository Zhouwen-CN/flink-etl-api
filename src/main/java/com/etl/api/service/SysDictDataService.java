package com.etl.api.service;

import com.etl.api.domain.entity.SysDictData;
import com.etl.api.domain.form.SysDictDataCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 字典数据表 服务层。
 *
 * @author chen
 * @since 2026-05-12
 */
public interface SysDictDataService extends IService<SysDictData> {

    ResponseVO<Void> addDictData(SysDictDataCreateForm form);
}
