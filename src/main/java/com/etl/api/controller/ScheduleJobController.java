package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.domain.form.ScheduleJobCreateForm;
import com.etl.api.domain.form.ScheduleJobStatusChangeForm;
import com.etl.api.domain.form.ScheduleJobUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.ScheduleJobVO;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.ScheduleJobService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/schedule")
@Tag(name = "定时任务 控制器")
@RequiredArgsConstructor
public class ScheduleJobController {
    private final ScheduleJobService scheduleJobService;
    private final EtlJobService etlJobService;

    @SaCheckPermission("schedule.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<ScheduleJobVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "任务名称") String searchName
    ) {

        val page = scheduleJobService.queryChain()
                .like(ScheduleJob::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), ScheduleJobVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("schedule.select")
    @Operation(summary = "ETL任务选择器")
    @GetMapping("/job/selector")
    public ResponseVO<List<DictionaryVO>> getEtlJobSelector() {
        // 只有batch模式才会有定时任务需求
        val vos = etlJobService.queryChain()
                .eq(EtlJob::getType, ETLJobTypeEnum.BATCH.getCode())
                .list()
                .stream().map(EtlJobConvert.INSTANCE::convert)
                .toList();

        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("schedule.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated ScheduleJobCreateForm form) {
        return scheduleJobService.addScheduleJob(form);
    }


    @SaCheckPermission("schedule.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated ScheduleJobUpdateForm form) {
        return scheduleJobService.modifyScheduleJob(form);
    }

    @SaCheckPermission("schedule.update")
    @Operation(summary = "更新状态")
    @PatchMapping
    public ResponseVO<Void> changeStatus(@RequestBody @Validated ScheduleJobStatusChangeForm form) {
        return scheduleJobService.changeStatus(form);
    }

    @SaCheckPermission("schedule.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return scheduleJobService.removeScheduleJob(id);
    }

    @SaCheckPermission("schedule.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return scheduleJobService.removeScheduleJobBatch(ids);
    }
}
