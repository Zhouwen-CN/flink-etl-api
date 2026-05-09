package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.PermissionConvert;
import com.etl.api.domain.entity.Permission;
import com.etl.api.domain.form.PermissionCreateForm;
import com.etl.api.domain.form.PermissionUpdateForm;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.PermissionVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.PermissionTypeEnum;
import com.etl.api.service.PermissionService;
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

@RestController
@RequestMapping("/permission")
@Tag(name = "权限 控制器")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @SaCheckPermission("permission.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<PermissionVO>> getPage(
            @RequestParam(value = "currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam(value = "pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "routeName", required = false) @Parameter(description = "路由名称") String routeName,
            @RequestParam(value = "code", required = false) @Parameter(description = "权限标识符") String code
    ) {
        val hasText = StringUtils.hasText(routeName);
        if (hasText) {
            routeName = routeName.toLowerCase();
        }
        val page = permissionService.queryChain()
                .eq(Permission::getType, PermissionTypeEnum.BUTTON.getCode())
                .like(Permission::getCode, code, StringUtils.hasText(code))
                .likeLeft(Permission::getCode, routeName, hasText)
                .pageAs(Page.of(currentPage, pageSize), PermissionVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("permission.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated PermissionCreateForm form) {
        return permissionService.addPermission(form);
    }

    @SaCheckPermission("permission.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated PermissionUpdateForm form) {
        val id = form.getId();
        if (id == 1L) {
            return ResponseVO.error("超级管理员 账号/角色/权限 禁止修改和删除");
        }
        val entity = PermissionConvert.INSTANCE.convert(form);
        permissionService.updateById(entity);
        return ResponseVO.ok();
    }

    @SaCheckPermission("permission.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        if (id == 1L) {
            return ResponseVO.error("超级管理员 账号/角色/权限 禁止修改和删除");
        }
        permissionService.removeById(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("permission.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        if (ids.contains(1L)) {
            return ResponseVO.error("超级管理员 账号/角色/权限 禁止修改和删除");
        }
        permissionService.removeByIds(ids);
        return ResponseVO.ok();
    }
}
