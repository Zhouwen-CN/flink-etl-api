package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.etl.api.domain.convert.SysLogConvert;
import com.etl.api.domain.entity.SysErrorLog;
import com.etl.api.domain.entity.SysLoginLog;
import com.etl.api.domain.entity.SysRequestLog;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysErrorLogVO;
import com.etl.api.domain.vo.SysLoginLogVO;
import com.etl.api.domain.vo.SysRequestLogVO;
import com.etl.api.service.SysErrorLogService;
import com.etl.api.service.SysLoginLogService;
import com.etl.api.service.SysRequestLogService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@Tag(name = "日志 控制器")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLoginLogService loginLogService;
    private final SysErrorLogService errorLogService;
    private final SysRequestLogService requestLogService;

    @SaCheckPermission("login-log.select")
    @Operation(summary = "登入日志分页查询")
    @GetMapping("/login")
    public ResponseVO<PageVO<SysLoginLogVO>> getSysLoginLogPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username
    ) {
        val page = loginLogService.queryChain()
                .like(SysLoginLog::getUsername, username, StringUtils.hasText(username))
                .orderBy(SysLoginLog::getCreateTime, false)
                .pageAs(Page.of(currentPage, pageSize), SysLoginLogVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }

    @SaCheckPermission("error-log.select")
    @Operation(summary = "异常日志分页查询")
    @GetMapping("/error")
    public ResponseVO<PageVO<SysErrorLogVO>> getErrorLogPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username
    ) {
        val page = errorLogService.queryChain()
                .like(SysErrorLog::getCreateUser, username, StringUtils.hasText(username))
                .orderBy(SysErrorLog::getCreateTime, false)
                .pageAs(Page.of(currentPage, pageSize), SysErrorLogVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }


    @SaCheckPermission("request-log.select")
    @Operation(summary = "异常日志分页查询")
    @GetMapping("/request")
    public ResponseVO<PageVO<SysRequestLogVO>> getRequestLogPage(
            @RequestParam("currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam("pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username,
            @RequestParam(value = "method", required = false) @Parameter(description = "请求方法") String method
    ) {

        val page = requestLogService.queryChain()
                .eq(SysRequestLog::getCreateUser, username, StringUtils.hasText(username))
                .eq(SysRequestLog::getRequestMethod, method, StringUtils.hasText(method))
                .orderBy(SysRequestLog::getTimestamp, false)
                .page(Page.of(currentPage, pageSize));

        val records = page.getRecords()
                .stream()
                .map(SysLogConvert.INSTANCE::convert)
                .toList();

        return ResponseVO.ok(new PageVO<>(records, page.getTotalRow()));
    }
}
