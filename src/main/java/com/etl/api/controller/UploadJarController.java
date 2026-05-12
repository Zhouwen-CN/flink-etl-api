package com.etl.api.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import com.etl.api.domain.entity.UploadJar;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.UploadJarVO;
import com.etl.api.service.UploadJarService;
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
public class UploadJarController {

    private final UploadJarService uploadJarService;


    @SaCheckPermission("jar.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<UploadJarVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "jar包名称") String searchName
    ) {
        val page = uploadJarService.queryChain()
                .like(UploadJar::getName, searchName, StringUtils.hasText(searchName))
                .pageAs(Page.of(currentPage, pageSize), UploadJarVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaIgnore
    @SaCheckPermission("jar.insert")
    @Operation(summary = "新增")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseVO<Void> add(
            @RequestParam("name") @Parameter(description = "jar包名称") String name,
            @RequestParam(value = "file", required = false) @Parameter(description = "jar包文件") MultipartFile file
    ) throws IOException {
        return uploadJarService.addJar(name, file);
    }

    @SaCheckPermission("jar.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        uploadJarService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("jar.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        uploadJarService.removeByIds(ids);
        return ResponseVO.ok();
    }
}
