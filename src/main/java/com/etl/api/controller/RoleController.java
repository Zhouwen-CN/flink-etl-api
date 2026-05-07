package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.entity.RolePermission;
import com.etl.api.domain.form.RoleCreateForm;
import com.etl.api.domain.form.RoleUpdateForm;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.PermissionSelectorVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.RoleVO;
import com.etl.api.service.PermissionService;
import com.etl.api.service.RolePermissionService;
import com.etl.api.service.RoleService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.val;
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

import static com.etl.api.domain.entity.table.RoleTableDef.ROLE;

@RestController
@RequestMapping("/role")
@Tag(name = "用户表 控制器")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;

    @SaCheckPermission("role.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<RoleVO>> getPage(
            @RequestParam(value = "currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam(value = "pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "searchName", required = false) @Parameter(description = "角色名|角色编码") String searchName
    ) {
        val page = roleService.queryChain()
                .where(ROLE.NAME.like(searchName).or(ROLE.CODE.like(searchName)))
                .pageAs(Page.of(currentPage, pageSize), RoleVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("role.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated RoleCreateForm form) {
        roleService.addRole(form);
        return ResponseVO.ok();
    }

    @SaCheckPermission("role.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated RoleUpdateForm form) {
        roleService.modifyRole(form);
        return ResponseVO.ok();
    }

    @SaCheckPermission("role.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        roleService.removeRole(id);
        return ResponseVO.ok();
    }

    @SaCheckPermission("role.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        roleService.removeRoleBatch(ids);
        return ResponseVO.ok();
    }

    @SaCheckPermission("role.select")
    @Operation(summary = "权限选择器查询")
    @GetMapping("/permission/selector")
    public ResponseVO<List<PermissionSelectorVO>> selector() {
        val vos = permissionService.list()
                .stream()
                .map(item -> {
                    val code = item.getCode();
                    val split = code.split("\\.");
                    if (split.length == 2) {
                        return PermissionSelectorVO.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .routeName(split[0])
                                .operationType(split[1])
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        return ResponseVO.ok(vos);
    }

    @SaCheckPermission("role.select")
    @Operation(summary = "角色权限信息查询")
    @GetMapping("/permission/{id}")
    public ResponseVO<List<Long>> getPermissionByRoleId(@PathVariable @Parameter(description = "角色ID") Long id) {
        val permissionIds = rolePermissionService.queryChain()
                .select(RolePermission::getPermissionId)
                .eq(RolePermission::getRoleId, id)
                .listAs(Long.class);
        return ResponseVO.ok(permissionIds);
    }
}
