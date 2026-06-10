package com.etl.api.enumeration;

import com.etl.api.domain.vo.DictionaryVO;
import com.mybatisflex.annotation.EnumValue;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

/**
 * Flink 任务状态枚举
 */
@Getter
public enum FlinkJobStatusEnum {
    UNKNOWN(0, "Unknown"),
    INITIALIZING(1, "Initializing"),
    CREATED(2, "Created"),
    RUNNING(3, "Running"),
    FAILING(4, "Failing"),
    FAILED(5, "Failed"),
    CANCELLING(6, "Cancelling"),
    CANCELED(7, "Canceled"),
    FINISHED(8, "Finished"),
    RESTARTING(9, "Restarting"),
    SUSPENDED(10, "Suspended"),
    RECONCILING(11, "Reconciling");

    @EnumValue
    private final int code;
    private final String desc;

    FlinkJobStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FlinkJobStatusEnum formName(String desc) {
        for (FlinkJobStatusEnum value : values()) {
            if (value.name().equals(desc)) {
                return value;
            }
        }
        return UNKNOWN;
    }

    /**
     * 正在处理的状态
     */
    public static List<Integer> getProcessingStatus() {
        return List.of(
                FlinkJobStatusEnum.INITIALIZING.code,
                FlinkJobStatusEnum.CREATED.code,
                FlinkJobStatusEnum.RUNNING.code,
                FlinkJobStatusEnum.FAILING.code,
                FlinkJobStatusEnum.CANCELLING.code,
                FlinkJobStatusEnum.RESTARTING.code
        );
    }

    /**
     * 状态选择器
     */
    public static List<DictionaryVO> toDictionaryVO() {
        return Stream.of(
                RUNNING,
                FAILED,
                CANCELED,
                FINISHED,
                RESTARTING,
                UNKNOWN
        ).map(item -> new DictionaryVO(item.desc, (long) item.code)).toList();
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
