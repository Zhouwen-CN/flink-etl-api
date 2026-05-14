package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.convert.FlinkClusterConvert;
import com.etl.api.domain.convert.JarPackageConvert;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.ETLJobInstanceVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
import com.etl.api.service.manager.JobManager;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/instance")
@Tag(name = "任务实例 控制器")
@RequiredArgsConstructor
public class ETLJobInstanceController {
    private final EtlJobInstanceService etlJobInstanceService;
    private final FlinkClusterService flinkClusterService;
    private final EtlJobService etlJobService;
    private final JarPackageService jarPackageService;
    private final JobManager jobManager;

    @SaCheckPermission("instance.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<ETLJobInstanceVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "instanceId", required = false) @Parameter(description = "任务实例id") String instanceId,
            @RequestParam(value = "clusterId", required = false) @Parameter(description = "集群id") Long clusterId,
            @RequestParam(value = "jobId", required = false) @Parameter(description = "任务id") Long jobId,
            @RequestParam(value = "jobType", required = false) @Parameter(description = "集群id") Integer jobType,
            @RequestParam(value = "status", required = false) @Parameter(description = "任务状态") Integer status
    ) {
        val page = etlJobInstanceService.queryChain()
                .like(EtlJobInstance::getId, instanceId, StringUtils.hasText(instanceId))
                .eq(EtlJobInstance::getClusterId, clusterId, Objects.nonNull(clusterId))
                .eq(EtlJobInstance::getJobId, jobId, Objects.nonNull(jobId))
                .eq(EtlJobInstance::getJobType, jobType, Objects.nonNull(jobType))
                .eq(EtlJobInstance::getStatus, status, Objects.nonNull(status))
                .orderBy(EtlJobInstance::getStatus, true)
                .orderBy(EtlJobInstance::getUpdateTime, false)
                .pageAs(Page.of(currentPage, pageSize), ETLJobInstanceVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("instance.delete")
    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") String id) {
        return etlJobInstanceService.removeInstance(id);
    }

    @SaCheckPermission("instance.select")
    @GetMapping("/cluster/selector")
    @Operation(summary = "Flink集群选择器")
    public ResponseVO<List<DictionaryVO>> getClusterSelector() {
        val vos = flinkClusterService.list()
                .stream().map(FlinkClusterConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("instance.select")
    @GetMapping("/job/selector")
    @Operation(summary = "任务选择器")
    public ResponseVO<List<DictionaryVO>> getJobSelector() {
        val vos = etlJobService.list()
                .stream().map(EtlJobConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("instance.select")
    @GetMapping("/status/selector")
    @Operation(summary = "任务状态选择器")
    public ResponseVO<List<DictionaryVO>> getStatusSelector() {
        val vos = FlinkJobStatusEnum.toDictionaryVO();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("instance.select")
    @GetMapping("/jar/selector")
    @Operation(summary = "任务状态选择器")
    public ResponseVO<List<DictionaryVO>> getJarSelector() {
        val vos = jarPackageService.list()
                .stream().map(JarPackageConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("instance.update")
    @PostMapping("/job/cancel/{id}")
    @Operation(summary = "停止任务实例")
    public ResponseVO<Void> cancelJob(@PathVariable @Parameter(description = "任务实例ID") String id) {
        return jobManager.cancelJob(id);
    }
}
