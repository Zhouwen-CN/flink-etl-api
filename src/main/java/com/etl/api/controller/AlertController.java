package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.Alert;
import com.etl.api.domain.entity.AlertJob;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.AlertCreateForm;
import com.etl.api.domain.form.AlertUpdateForm;
import com.etl.api.domain.vo.AlertVO;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.service.AlertJobService;
import com.etl.api.service.AlertService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.manager.SendMailManager;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/alert")
@Tag(name = "告警 控制器")
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;
    private final AlertJobService alertJobService;
    private final EtlJobService etlJobService;
    private final SendMailManager sendMailManager;


    @SaCheckPermission("alert.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<AlertVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "告警名称") String searchName
    ) {
        val page = alertService.queryChain()
                .like(Alert::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), AlertVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("alert.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated AlertCreateForm form) {
        return alertService.addAlert(form);
    }

    @SaCheckPermission("alert.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated AlertUpdateForm form) {
        return alertService.modifyAlert(form);
    }

    @SaCheckPermission("alert.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return alertService.removeAlert(id);
    }

    @SaCheckPermission("alert.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return alertService.removeBatchAlert(ids);
    }

    @SaCheckPermission("alert.select")
    @GetMapping("/job/selector")
    @Operation(summary = "任务状态选择器")
    public ResponseVO<List<DictionaryVO>> getStatusSelector() {
        val selectorList = etlJobService.queryChain()
                .eq(EtlJob::getType, ETLJobTypeEnum.STREAMING.getCode())
                .list()
                .stream()
                .map(EtlJobConvert.INSTANCE::convert)
                .toList();

        return ResponseVO.ok(selectorList);
    }

    @SaCheckPermission("alert.select")
    @GetMapping("/job/{id}")
    @Operation(summary = "获取告警任务id列表")
    public ResponseVO<List<Long>> getStatusSelector(@PathVariable @Parameter(description = "ID") Long id) {
        val jobIds = alertJobService.queryChain()
                .select(AlertJob::getJobId)
                .eq(AlertJob::getAlertId, id)
                .listAs(Long.class);
        return ResponseVO.ok(jobIds);
    }

    @SaCheckPermission("alert.select")
    @GetMapping("/test/{id}")
    @Operation(summary = "邮件测试")
    public ResponseVO<Void> testSend(@PathVariable @Parameter(description = "ID") Long id) {
        val alert = alertService.queryChain()
                .eq(Alert::getId, id)
                .one();

        if (alert == null) {
            return ResponseVO.recordNotFoundError(id);
        }

        sendMailManager.testSend(alert.getName(), alert.getEmail());
        alert.setSendTime(LocalDateTime.now());
        alertService.updateById(alert);
        return ResponseVO.ok();
    }
}
