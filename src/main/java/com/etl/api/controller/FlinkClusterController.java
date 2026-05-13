package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.FlinkClusterConvert;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.form.FlinkClusterCreateForm;
import com.etl.api.domain.form.FlinkClusterUpdateForm;
import com.etl.api.domain.vo.FlinkClusterVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.FlinkClusterService;
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

@Validated
@RestController
@RequestMapping("/flink/cluster")
@Tag(name = "Flink集群 控制器")
@RequiredArgsConstructor
public class FlinkClusterController {

    private final FlinkClusterService flinkClusterService;

    @SaCheckPermission("cluster.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<FlinkClusterVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "name", required = false) @Parameter(description = "集群名称") String name
    ) {
        val page = flinkClusterService.queryChain()
                .like(FlinkCluster::getName, name, StringUtils.hasText(name))
                .pageAs(Page.of(currentPage, pageSize), FlinkClusterVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("cluster.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated FlinkClusterCreateForm form) {
        return flinkClusterService.addCluster(form);
    }

    @SaCheckPermission("cluster.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated FlinkClusterUpdateForm form) {
        val entity = FlinkClusterConvert.INSTANCE.convert(form);
        flinkClusterService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("cluster.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return flinkClusterService.removeCluster(id);
    }

    @SaCheckPermission("cluster.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return flinkClusterService.removeClusterBatch(ids);
    }
}
