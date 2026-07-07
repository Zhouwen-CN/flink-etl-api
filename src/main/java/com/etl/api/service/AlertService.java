package com.etl.api.service;

import com.etl.api.domain.entity.Alert;
import com.etl.api.domain.form.AlertCreateForm;
import com.etl.api.domain.form.AlertUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.Collection;

/**
 * 告警表 服务层。
 *
 * @author chen
 * @since 2026-07-07
 */
public interface AlertService extends IService<Alert> {

    ResponseVO<Void> addAlert(AlertCreateForm form);

    ResponseVO<Void> modifyAlert(AlertUpdateForm form);

    ResponseVO<Void> removeAlert(Long id);

    ResponseVO<Void> removeBatchAlert(Collection<Long> ids);
}
