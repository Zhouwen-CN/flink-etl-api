package com.etl.api.controller;

import com.etl.api.domain.vo.LoginLogVO;
import com.etl.api.domain.vo.PageVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.LoginLogService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.etl.api.domain.entity.table.LoginLogTableDef.LOGIN_LOG;
import static com.etl.api.domain.entity.table.UserTableDef.USER;

@RestController
@RequestMapping("/log")
@Tag(name = "日志 控制器")
@RequiredArgsConstructor
public class LogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询")
    @GetMapping("/login/{pageSize}/{pageNumber}")
    public ResponseVO<PageVO<LoginLogVO>> getPage(
            @PathVariable @Parameter(description = "页面大小") @Min(1) @Max(20) Integer pageSize,
            @PathVariable @Parameter(description = "当前页面") @Min(1) Integer pageNumber
    ) {
        val queryWrapper = QueryWrapper.create()
                .select(
                        LOGIN_LOG.ID,
                        USER.USERNAME,
                        LOGIN_LOG.OPERATION,
                        LOGIN_LOG.IP,
                        LOGIN_LOG.REGION,
                        LOGIN_LOG.CREATE_TIME
                )
                .from(LOGIN_LOG)
                .join(USER)
                .on(LOGIN_LOG.USER_ID.eq(USER.ID))
                .orderBy(LOGIN_LOG.CREATE_TIME, false);

        val page = loginLogService.pageAs(Page.of(pageNumber, pageSize), queryWrapper, LoginLogVO.class);
        return ResponseVO.ok(PageVO.from(page));
    }
}
