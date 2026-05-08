package com.etl.api.controller;

import com.etl.api.domain.entity.LoginLog;
import com.etl.api.domain.vo.LoginLogVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.LoginLogService;
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
public class LogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询")
    @GetMapping("/login")
    public ResponseVO<PageVO<LoginLogVO>> getPage(
            @RequestParam(value = "currentPage") @Parameter(description = "当前页面") @Min(1) Integer currentPage,
            @RequestParam(value = "pageSize") @Parameter(description = "页面大小") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "username", required = false) @Parameter(description = "用户名") String username
    ) {
        val page = loginLogService.queryChain()
                .like(LoginLog::getUsername, username, StringUtils.hasText(username))
                .orderBy(LoginLog::getCreateTime, false)
                .pageAs(Page.of(currentPage, pageSize), LoginLogVO.class);

        return ResponseVO.ok(PageVO.from(page));
    }
}
