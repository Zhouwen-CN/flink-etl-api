package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.convert.FlinkClusterConvert;
import com.etl.api.domain.convert.JarPackageConvert;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.form.EtlJobUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.ETLJobVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
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

@RestController
@RequestMapping("/etl/job")
@Tag(name = "ETL任务 控制器")
@RequiredArgsConstructor
public class ETLJobController {

    private final EtlJobService etlJobService;
    private final JarPackageService jarPackageService;
    private final FlinkClusterService flinkClusterService;

    @SaCheckPermission("job.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<ETLJobVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "任务名称") String searchName
    ) {
        val page = etlJobService.queryChain()
                .like(EtlJob::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), ETLJobVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("job.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated EtlJobCreateForm form) {
        return etlJobService.addEtlJob(form);
    }

    @SaCheckPermission("job.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated EtlJobUpdateForm form) {
        val entity = EtlJobConvert.INSTANCE.convert(form);
        etlJobService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("job.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return etlJobService.removeJob(id);
    }

    @SaCheckPermission("job.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return etlJobService.removeJobBatch(ids);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "jar包选择器")
    @GetMapping("/jar/selector")
    public ResponseVO<List<DictionaryVO>> jarSelector() {
        val vos = jarPackageService.list()
                .stream().map(JarPackageConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("job.select")
    @Operation(summary = "集群选择器")
    @GetMapping("/cluster/selector")
    public ResponseVO<List<DictionaryVO>> clusterSelector() {
        val vos = flinkClusterService.list()
                .stream().map(FlinkClusterConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("job.update")
    @Operation(summary = "运行任务")
    @GetMapping("/{id}")
    public ResponseVO<Void> runJob(@PathVariable @Parameter(description = "ID") Long id) {
        return etlJobService.runJob(id);
    }
}
