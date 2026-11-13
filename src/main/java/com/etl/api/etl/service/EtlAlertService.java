package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlAlert;
import com.etl.api.etl.domain.form.EtlAlertCreateForm;
import com.etl.api.etl.domain.form.EtlAlertUpdateForm;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 告警表 服务层。
 *
 * @author chen
 * @since 2026-07-07
 */
public interface EtlAlertService extends IService<EtlAlert> {

    ResponseVO<Void> addAlert(EtlAlertCreateForm form);

    ResponseVO<Void> modifyAlert(EtlAlertUpdateForm form);

    ResponseVO<Void> removeAlert(Long id);

    ResponseVO<Void> removeBatchAlert(Collection<Long> ids);
}
