package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.etl.api.domain.convert.UserConvert;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.entity.UserRole;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.UserRoleVO;
import com.etl.api.domain.vo.UserVO;
import com.etl.api.exception.RecordAlreadyExistsException;
import com.etl.api.exception.RecordNotFoundException;
import com.etl.api.service.UserRoleService;
import com.etl.api.service.UserService;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
@RequestMapping("/user")
@Tag(name = "用户表 控制器")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRoleService userRoleService;

    @SaCheckPermission("user.select")
    @Operation(summary = "分页查询")
    @GetMapping
    public ResponseVO<PageVO<UserVO>> getPage(
            @RequestParam(value = "currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam(value = "pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username
    ) {
        val page = userService.queryChain()
                .like(User::getUsername, username, StringUtils.hasText(username))
                .pageAs(Page.of(currentPage, pageSize), UserVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("user.select")
    @Operation(summary = "ID查询")
    @GetMapping("/{id}")
    public ResponseVO<UserVO> getById(@PathVariable @Parameter(description = "ID") @Min(1) Long id) {
        val userVO = userService.queryChain()
                .eq(User::getId, id)
                .oneAs(UserVO.class);

        if (userVO == null) {
            throw new RecordNotFoundException(id);
        }

        return ResponseVO.ok(userVO);
    }

    @SaCheckPermission("user.insert")
    @Operation(summary = "新增")
    @PostMapping
    public ResponseVO<Void> add(@RequestBody @Validated UserCreateForm form) {
        val username = form.getUsername();
        val exists = userService.queryChain()
                .eq(User::getUsername, username)
                .exists();

        if (exists) {
            throw new RecordAlreadyExistsException(username);
        }

        val user = UserConvert.INSTANCE.convert(form);
        userService.save(user);
        return ResponseVO.ok();
    }

    @SaCheckPermission("user.update")
    @Operation(summary = "更新")
    @PutMapping
    public ResponseVO<Void> modify(@RequestBody @Validated UserUpdateForm form) {
        val user = UserConvert.INSTANCE.convert(form);
        userService.updateById(user);
        return ResponseVO.ok();
    }

    @SaCheckPermission("user.delete")
    @Operation(summary = "删除")
    @DeleteMapping("/{id}")
    public ResponseVO<Void> delete(@PathVariable @Parameter(description = "ID") Long id) {
        userService.removeById(id);
        userRoleService.remove(
                QueryWrapper.create().eq(UserRole::getUserId, id)
        );
        return ResponseVO.ok();
    }

    @SaCheckPermission("user.delete")
    @Operation(summary = "批量删除")
    @DeleteMapping
    public ResponseVO<Void> batchDelete(@RequestParam("ids") @Parameter(description = "ID列表") @Size(min = 1, max = 50) Collection<Long> ids) {
        userService.removeByIds(ids);
        userRoleService.remove(
                QueryWrapper.create().in(UserRole::getUserId, ids)
        );
        return ResponseVO.ok();
    }

    @Operation(summary = "获取用户权限信息")
    @GetMapping("/info")
    public ResponseVO<UserRoleVO> getUserPermissionInfo() {
        val userRoleVO = new UserRoleVO(
                StpUtil.getLoginIdAsLong(),
                SaSessionUtil.getUsername(),
                SaSessionUtil.getNickname(),
                StpUtil.getRoleList(),
                StpUtil.getPermissionList()
        );
        return ResponseVO.ok(userRoleVO);
    }
}
