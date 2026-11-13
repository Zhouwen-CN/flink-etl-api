package com.etl.api.sys.service;

import com.etl.api.sys.domain.entity.SysDictData;
import com.etl.api.sys.domain.form.SysDictDataCreateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
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
