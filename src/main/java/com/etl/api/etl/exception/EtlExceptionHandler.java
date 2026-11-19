package com.etl.api.etl.exception;

import com.etl.api.sys.domain.vo.ResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class EtlExceptionHandler {

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
    @ExceptionHandler(ScheduledTaskException.class)
    public ResponseVO<Void> scheduleJobExceptionHandler(ScheduledTaskException e) {
        return ResponseVO.error(e.getMessage());
    }
}
