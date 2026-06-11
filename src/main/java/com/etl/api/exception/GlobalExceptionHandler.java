package com.etl.api.exception;


import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * <pre>
 * 全局异常处理
 *   200：成功
 *   201：请求成功，并创建一个新的资源，通常是post、put请求相应
 *   202：请求已收到，但是未采取行动
 *   204：删除成功
 *   400：请求有误
 *   401：没有权限
 *   403：禁止访问
 *   404：资源不存在
 *   410：记录被删除
 *   422：参数错误
 *   500：服务器错误
 * </pre>
 *
 * @author chen
 * @since 2026-04-28
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorLogService errorLogService;

    /**
     * 请求体参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO<Void> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        val bindingResult = e.getBindingResult();

        val fieldError = bindingResult.getFieldError();
        if (fieldError != null) {
            return ResponseVO.error(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s %s", fieldError.getField(), fieldError.getDefaultMessage()));
        }

        val globalError = bindingResult.getGlobalError();
        if (globalError != null) {
            return ResponseVO.error(HttpStatus.UNPROCESSABLE_ENTITY, globalError.getDefaultMessage());
        }

        return ResponseVO.error(HttpStatus.UNPROCESSABLE_ENTITY, "请求体参数校验失败");
    }

    /**
     * 请求URL参数校验异常
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseVO<Void> handlerMethodValidationExceptionHandler(HandlerMethodValidationException e) {
        val allValidationResults = e.getParameterValidationResults();
        if (CollectionUtils.isEmpty(allValidationResults)) {
            return ResponseVO.error(HttpStatus.UNPROCESSABLE_ENTITY, e.getReason());
        }
        val parameterValidationResult = allValidationResults.get(0);
        val parameterName = parameterValidationResult.getMethodParameter().getParameterName();
        val resolvableErrors = parameterValidationResult.getResolvableErrors();
        var defaultMessage = "请求URL参数校验失败";
        if (!CollectionUtils.isEmpty(resolvableErrors)) {
            defaultMessage = resolvableErrors.get(0).getDefaultMessage();
        }
        return ResponseVO.error(HttpStatus.UNPROCESSABLE_ENTITY, String.format("%s %s", parameterName, defaultMessage));
    }

    /**
     * 未认证，返回401
     */
    @ExceptionHandler(NotLoginException.class)
    public ResponseVO<Void> notLoginExceptionHandler() {
        return ResponseVO.error(HttpStatus.UNAUTHORIZED, "未认证");
    }

    /**
     * 未授权，返回403
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseVO<Void> notPermissionExceptionHandler() {
        return ResponseVO.error(HttpStatus.FORBIDDEN, "未授权");
    }

    /**
     * 资源未找到，返回404
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseVO<Void> noResourceFoundExceptionHandler(NoResourceFoundException e) {
        return ResponseVO.error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * Flink API 请求异常
     */
    @ExceptionHandler(FlinkApiRequestException.class)
    public ResponseVO<Void> flinkApiRequestExceptionHandler(FlinkApiRequestException e) {
        return ResponseVO.error(e.getMessage());
    }

    /**
     * ETL 任务异常
     */
    @ExceptionHandler(EtlJobException.class)
    public ResponseVO<Void> etlJobExceptionHandler(EtlJobException e) {
        return ResponseVO.error(e.getMessage());
    }

    /**
     * 调度任务异常
     */
    @ExceptionHandler(ScheduleJobException.class)
    public ResponseVO<Void> scheduleJobExceptionHandler(ScheduleJobException e) {
        return ResponseVO.error(e.getMessage());
    }

    /**
     * 全局兜底异常处理
     */
    @ExceptionHandler
    public ResponseEntity<ResponseVO<Void>> handlerException(Exception e, HttpServletRequest request) {
        if (!"/applications".equals(request.getRequestURI())) {
            errorLogService.saveErrorLog(e);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ResponseVO.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }
}
