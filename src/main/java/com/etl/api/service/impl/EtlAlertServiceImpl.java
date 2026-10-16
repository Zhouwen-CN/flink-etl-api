package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlAlertConvert;
import com.etl.api.domain.entity.EtlAlert;
import com.etl.api.domain.entity.EtlAlertJob;
import com.etl.api.domain.form.EtlAlertCreateForm;
import com.etl.api.domain.form.EtlAlertUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlAlertMapper;
import com.etl.api.service.EtlAlertJobService;
import com.etl.api.service.EtlAlertService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 告警表 服务层实现。
 *
 * @author chen
 * @since 2026-07-07
 */
@Service
@RequiredArgsConstructor
public class EtlAlertServiceImpl extends ServiceImpl<EtlAlertMapper, EtlAlert> implements EtlAlertService {

    private final EtlAlertJobService alertJobService;

    @Override
    public ResponseVO<Void> addAlert(EtlAlertCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlAlert::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = EtlAlertConvert.INSTANCE.convert(form);
        this.save(entity);
        this.saveAlertJobList(form.getJobIds(), entity.getId(), false);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyAlert(EtlAlertUpdateForm form) {
        val entity = EtlAlertConvert.INSTANCE.convert(form);
        this.updateById(entity);
        this.saveAlertJobList(form.getJobIds(), entity.getId(), true);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeAlert(Long id) {
        this.removeById(id);
        this.removeAlertJobList(List.of(id));
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeBatchAlert(Collection<Long> ids) {
        this.removeByIds(ids);
        this.removeAlertJobList(ids);
        return ResponseVO.ok();
    }

    private void removeAlertJobList(Collection<Long> alertIds) {
        alertJobService.updateChain()
                .in(EtlAlertJob::getAlertId, alertIds)
                .remove();
    }

    private void saveAlertJobList(List<Long> jobIds, Long alertId, boolean isUpdate) {
        val alertJobList = jobIds
                .stream()
                .map(jobId ->
                        EtlAlertJob.builder()
                                .alertId(alertId)
                                .jobId(jobId)
                                .build()
                ).toList();

        if (isUpdate) {
            this.removeAlertJobList(List.of(alertId));
        }

        alertJobService.saveBatch(alertJobList);
    }
}
