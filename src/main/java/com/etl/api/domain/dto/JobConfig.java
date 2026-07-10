package com.etl.api.domain.dto;

import com.etl.api.domain.entity.EtlJob;
import com.etl.api.enumeration.ETLJobTypeEnum;
import com.etl.api.exception.EtlJobException;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class JobConfig {
    private Job job = new Job();
    private List<Source> sources = new ArrayList<>();
    private List<Transform> transforms = new ArrayList<>();
    private List<Sink> sinks = new ArrayList<>();

    /**
     * 校验任务配置，拷贝至 flink-etl-tool：com.etl.core.config.ConfigParser#validate
     */
    public void validate() throws EtlJobException {
        val outputTables = new HashSet<>();

        // 校验 sources 数组
        if (sources == null || sources.isEmpty()) {
            throw new EtlJobException("缺少 sources 配置");
        }
        for (int i = 0; i < sources.size(); i++) {
            val source = sources.get(i);
            if (source.getType() == null || source.getType().isEmpty()) {
                throw new EtlJobException("缺少 sources[" + i + "].type 配置");
            }
            if (source.getOutputTable() == null || source.getOutputTable().isEmpty()) {
                throw new EtlJobException("缺少 sources[" + i + "].outputTable 配置");
            }
            if (!outputTables.add(source.getOutputTable())) {
                throw new EtlJobException("sources 中 outputTable 重复: " + source.getOutputTable());
            }
        }

        // 校验 transforms
        if (transforms != null) {
            for (int i = 0; i < transforms.size(); i++) {
                val transform = transforms.get(i);
                if (transform.getType() == null || transform.getType().isEmpty()) {
                    throw new EtlJobException("缺少 transforms[" + i + "].type 配置");
                }
                // 因为只有 sql transform，inputTable 其实在sql中，所以不需要检验
                /*if (transform.getInputTable() == null || transform.getInputTable().isEmpty()) {
                    throw new EtlJobException("缺少 transforms[" + i + "].inputTable 配置");
                }
                if (!outputTables.contains(transform.getInputTable())) {
                    throw new EtlJobException("transforms[" + i + "].inputTable '"
                            + transform.getInputTable() + "' 未在上游 outputTable 中定义");
                }*/
                if (transform.getOutputTable() == null || transform.getOutputTable().isEmpty()) {
                    throw new EtlJobException("缺少 transforms[" + i + "].outputTable 配置");
                }
                if (!outputTables.add(transform.getOutputTable())) {
                    throw new EtlJobException("transforms 中 outputTable 重复或与 sources 冲突: " + transform.getOutputTable());
                }
            }
        }

        // 校验 sinks 数组
        if (sinks == null || sinks.isEmpty()) {
            throw new EtlJobException("缺少 sinks 配置");
        }
        for (int i = 0; i < sinks.size(); i++) {
            val sink = sinks.get(i);
            if (sink.getType() == null || sink.getType().isEmpty()) {
                throw new EtlJobException("缺少 sinks[" + i + "].type 配置");
            }
            if (sink.getInputTable() == null || sink.getInputTable().isEmpty()) {
                throw new EtlJobException("缺少 sinks[" + i + "].inputTable 配置");
            }
            // 验证 inputTable 是否在上游定义
            if (!outputTables.contains(sink.getInputTable())) {
                throw new EtlJobException("sinks[" + i + "].inputTable '" + sink.getInputTable()
                        + "' 未在上游 source.outputTable 或 transform.outputTable 中定义");
            }
        }
    }

    @Getter
    @Setter
    public static class Job {
        private String name;
        private String mode;
        private Integer parallelism;
        private Integer checkpointInterval = 30000;
        private Integer checkpointTimeout = 60000;

        public void from(EtlJob etlJob) {
            val etlJobTypeEnum = ETLJobTypeEnum.from(etlJob.getType());
            this.name = etlJob.getName();
            this.mode = etlJobTypeEnum.getDesc();
            this.parallelism = etlJob.getParallelism();

            // 检查点间隔 * 2 = 检查点超时
            val checkpointInterval = etlJob.getCheckpointInterval();
            if (etlJobTypeEnum == ETLJobTypeEnum.STREAMING && checkpointInterval != null) {
                this.checkpointInterval = checkpointInterval;
                this.checkpointTimeout = checkpointInterval * 2;
            }
        }
    }

    @Getter
    @Setter
    public static class Source {
        private String type;
        private String outputTable;
        private Map<String, Object> config;
    }

    @Getter
    @Setter
    public static class Transform {
        private String type;
        private String inputTable;
        private String outputTable;
        private Map<String, Object> config;
    }

    @Getter
    @Setter
    public static class Sink {
        private String type;
        private String inputTable;
        private Map<String, Object> config;
    }
}
