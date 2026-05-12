package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.convert.RoleConvert;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.entity.UserRole;
import com.etl.api.domain.form.ChangePwdForm;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.UserRoleVO;
import com.etl.api.domain.vo.UserVO;
import com.etl.api.service.RoleService;
import com.etl.api.service.UserRoleService;
import com.etl.api.service.UserService;
import com.etl.api.util.AESUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
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
@RequestMapping("/user")
@Tag(name = "用户表 控制器")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    @Value("${custom.default-pwd}")
    private String defaultPwd;

    @SaCheckPermission("user.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<UserVO>> getPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username
    ) {
        val page = userService.queryChain()
                .like(User::getUsername, username, StringUtils.hasText(username))
                .pageAs(Page.of(currentPage, pageSize), UserVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("user.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated UserCreateForm form) {
        return userService.addUser(form);
    }

    @SaCheckPermission("user.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated UserUpdateForm form) {
        return userService.modifyUser(form);
    }

    @SaCheckPermission("user.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> remove(@PathVariable @Parameter(description = "ID") Long id) {
        return userService.removeUser(id);
    }

    @SaCheckPermission("user.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> removeBatch(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        return userService.removeUserBatch(ids);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public ResponseVO<UserRoleVO> getUserInfo() {
        val userRoleVO = new UserRoleVO(
                StpUtil.getLoginIdAsLong(),
                SaSessionUtil.getUsername(),
                SaSessionUtil.getNickname(),
                StpUtil.getRoleList(),
                StpUtil.getPermissionList()
        );
        return ResponseVO.ok(userRoleVO);
    }

    @SaCheckPermission("user.select")
    @Operation(summary = "用户角色信息")
    @GetMapping("/role/{id}")
    public ResponseVO<List<Long>> getRoleByUserId(@PathVariable @Parameter(description = "ID") Long id) {
        val roleIds = userRoleService.queryChain()
                .select(UserRole::getRoleId)
                .eq(UserRole::getUserId, id)
                .listAs(Long.class);
        return ResponseVO.ok(roleIds);
    }

    @SaCheckPermission("user.select")
    @Operation(summary = "角色选择器")
    @GetMapping("/role/selector")
    public ResponseVO<List<DictionaryVO>> selector() {
        val vos = roleService.list()
                .stream()
                .map(RoleConvert.INSTANCE::convert)
                .toList();
        return ResponseVO.ok(vos);
    }

    @PatchMapping("/pwd/change")
    public ResponseVO<Void> changePwd(@RequestBody @Validated ChangePwdForm form) {
        return userService.changePwd(form);
    }

    @SaCheckRole("admin")
    @PatchMapping("/pwd/reset/{id}")
    public ResponseVO<Void> resetPwd(@PathVariable @Parameter(description = "ID") Long id) {
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }
        userService.updateChain()
                .eq(User::getId, id)
                .set(User::getPassword, AESUtil.encrypt(defaultPwd))
                .update();
        StpUtil.logout(id);
        return ResponseVO.ok();
    }
}
