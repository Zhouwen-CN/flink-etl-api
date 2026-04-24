package com.etl.api.exception;


import com.etl.api.domain.vo.ResponseVO;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
     * DML操作异常
     */
    @ExceptionHandler(DMLException.class)
    public ResponseVO<Void> dmlFailureExceptionHandler(DMLException e) {
        return ResponseVO.error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

}
