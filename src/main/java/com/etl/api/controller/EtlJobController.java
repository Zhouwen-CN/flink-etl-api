package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.aop.IgnoreTenant;
import com.etl.api.domain.convert.EtlFlinkClusterConvert;
import com.etl.api.domain.convert.EtlJarPackageConvert;
import com.etl.api.domain.convert.EtlProjectConvert;
import com.etl.api.domain.entity.EtlFlinkCheckpointSync;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobConfigRollbackForm;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.form.EtlJobSubmitForm;
import com.etl.api.domain.form.EtlJobUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.EtlFlinkCheckpointVO;
import com.etl.api.domain.vo.EtlJobConfigVersionVO;
import com.etl.api.domain.vo.EtlJobVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.EtlFlinkCheckpointSyncService;
import com.etl.api.service.EtlFlinkClusterService;
import com.etl.api.service.EtlJarPackageService;
import com.etl.api.service.EtlJobConfigVersionService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.EtlProjectService;
import com.etl.api.service.manager.EtlJobManager;
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/job")
@Tag(name = "ETL任务 控制器")
@RequiredArgsConstructor
public class EtlJobController {

    private final EtlJobService jobService;
    private final EtlJobConfigVersionService configVersionService;
    private final EtlJarPackageService jarPackageService;
    private final EtlFlinkClusterService flinkClusterService;
    private final EtlJobManager jobManager;
    private final EtlFlinkCheckpointSyncService flinkCheckpointSyncService;
    private final EtlProjectService projectService;

    @SaCheckPermission("job.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<EtlJobVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "name", required = false) @Parameter(description = "任务名称") String name,
            @RequestParam(value = "type", required = false) @Parameter(description = "任务类型") Integer type,
            @RequestParam(value = "projectId", required = false) @Parameter(description = "项目id") Long projectId
    ) {
        val page = jobService.queryChain()
                .like(EtlJob::getName, name, StringUtils.hasText(name))
                .eq(EtlJob::getType, type, Objects.nonNull(type))
                .eq(EtlJob::getProjectId, projectId, Objects.nonNull(projectId))
                .pageAs(Page.of(currentPage, pageSize), EtlJobVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("job.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated EtlJobCreateForm form) {
        return jobService.addJob(form);
    }

    @IgnoreTenant
    @SaCheckPermission("job.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated EtlJobUpdateForm form) {
        return jobService.updateJob(form);
    }

    @SaCheckPermission("job.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return jobService.removeJob(id);
    }

    @SaCheckPermission("job.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return jobService.removeJobBatch(ids);
    }

    @SaCheckPermission("job.insert")
    @Operation(summary = "运行任务")
    @PostMapping("/run")
    public ResponseVO<Void> runJob(@RequestBody @Validated EtlJobSubmitForm form) {
        jobManager.runJob(form.getId(), form.getSavepointPath(), form.getAllowNonRestoredState());
        return ResponseVO.ok();
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "配置版本列表")
    @GetMapping("/config/version")
    public ResponseVO<List<EtlJobConfigVersionVO>> configVersions(
            @RequestParam("jobId") @Parameter(description = "任务ID") Long jobId
    ) {
        return configVersionService.listByJobId(jobId);
    }

    @SaCheckPermission("job.update")
    @Operation(summary = "配置回滚")
    @PutMapping("/config/rollback")
    public ResponseVO<Void> rollbackConfig(@RequestBody @Validated EtlJobConfigRollbackForm form) {
        return configVersionService.rollback(form);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "jar包选择器")
    @GetMapping("/jar/selector")
    public ResponseVO<List<DictionaryVO>> jarSelector() {
        val vos = jarPackageService.list()
                .stream().map(EtlJarPackageConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "项目选择器")
    @GetMapping("/project/selector")
    public ResponseVO<List<DictionaryVO>> projectSelector() {
        val vos = projectService.list()
                .stream()
                .map(EtlProjectConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "集群选择器")
    @GetMapping("/cluster/selector")
    public ResponseVO<List<DictionaryVO>> clusterSelector() {
        val vos = flinkClusterService.list()
                .stream().map(EtlFlinkClusterConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "检查点历史选择器")
    @GetMapping("/checkpoint/selector")
    public ResponseVO<List<EtlFlinkCheckpointVO>> checkpointSelector(
            @RequestParam("jobId") @Parameter(description = "任务ID") String jobId
    ) {
        val vos = flinkCheckpointSyncService.queryChain()
                .eq(EtlFlinkCheckpointSync::getJobId, jobId)
                .orderBy(EtlFlinkCheckpointSync::getTriggerTime, false)
                .limit(10) // 只输出10条
                .listAs(EtlFlinkCheckpointVO.class);
        return ResponseVO.ok(vos);
    }
}
