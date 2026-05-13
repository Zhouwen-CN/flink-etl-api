package com.etl.api.enumeration;

import java.util.List;

public enum FlinkJobStatusEnum {
    UNKNOWN,
    INITIALIZING,
    CREATED,
    RUNNING,
    FAILING,
    FAILED,
    CANCELLING,
    CANCELED,
    FINISHED,
    RESTARTING,
    SUSPENDED,
    RECONCILING;

    public static FlinkJobStatusEnum fromValue(String value) {
        for (FlinkJobStatusEnum status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNKNOWN;
    }


    public static List<String> getMonitorStatus() {
        return List.of(
                FlinkJobStatusEnum.INITIALIZING.name(),
                FlinkJobStatusEnum.CREATED.name(),
                FlinkJobStatusEnum.RUNNING.name(),
                FlinkJobStatusEnum.FAILING.name(),
                FlinkJobStatusEnum.CANCELLING.name(),
                FlinkJobStatusEnum.RESTARTING.name()
        );
    }
}
