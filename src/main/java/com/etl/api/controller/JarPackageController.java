package com.etl.api.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.entity.JarPackage;
import com.etl.api.domain.vo.JarPackageVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
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
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;

@RestController
@RequestMapping("/jar")
@Tag(name = "jar包 控制器")
@RequiredArgsConstructor
public class JarPackageController {

    private final JarPackageService jarPackageService;


    @SaCheckPermission("jar.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<JarPackageVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "jar包名称") String searchName
    ) {
        val page = jarPackageService.queryChain()
                .like(JarPackage::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), JarPackageVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("jar.insert")
    @Operation(summary = "新增")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO<Void> add(
            @RequestParam("name") @Parameter(description = "jar包名称") String name,
            @RequestParam(value = "file", required = false) @Parameter(description = "jar包文件") MultipartFile file
    ) throws IOException {
        return jarPackageService.addJar(name, file);
    }

    @SaCheckPermission("jar.update")
    @Operation(summary = "更新")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO<Void> modify(
            @RequestParam("id") @Parameter(description = "ID") Long id,
            @RequestParam("name") @Parameter(description = "jar包名称") String name,
            @RequestParam(value = "file", required = false) @Parameter(description = "jar包文件") MultipartFile file
    ) {
        return jarPackageService.modifyJar(id, name, file);
    }

    @SaCheckPermission("jar.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return jarPackageService.removeJar(id);
    }

    @SaCheckPermission("jar.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return jarPackageService.removeJarBatch(ids);
    }
}
