package com.etl.api.service.manager;

import com.etl.api.domain.entity.ClusterUploadedJar;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.vo.DictionaryVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.exception.EtlJobException;
import com.etl.api.service.ClusterUploadedJarService;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
import com.etl.api.service.JobVariableService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.etl.api.util.LocalDateTimeUtil;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EtlJobManager {

    // 服务器时间容忍3分钟误差，通常内网的服务器时间会慢
    private static final Duration UPLOADED_OFFSET = Duration.ofMinutes(3);
    private final EtlJobService etlJobService;
    private final FlinkClusterService flinkClusterService;
    private final JarPackageService jarPackageService;
    private final ClusterUploadedJarService clusterUploadedJarService;
    private final FlinkApiProvider flinkApiProvider;
    private final EtlJobInstanceService etlJobInstanceService;
    private final JobVariableService jobVariableService;

    public void runJob(Long jobId, @Nullable String savepointPath) {
        val etlJob = etlJobService.getById(jobId);
        if (etlJob == null) {
            throw new EtlJobException("Etl 任务未找到: " + jobId);
        }

        // 是否有正在运行的任务
        val exists = etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getJobId, jobId)
                .in(EtlJobInstance::getStatus, FlinkJobStatusEnum.getProcessingStatus())
                .exists();
        if (exists) {
            throw new EtlJobException("运行失败，尚有正在运行的任务");
        }

        // flink集群信息
        val clusterId = etlJob.getClusterId();
        val flinkCluster = flinkClusterService.getById(clusterId);
        if (flinkCluster == null) {
            throw new EtlJobException("Flink 集群未找到: " + clusterId);
        }

        // 检查集群状态
        val status = flinkCluster.getStatus();
        if (!status) {
            throw new EtlJobException("运行失败，集群已禁用: " + flinkCluster.getName());
        }
        val jobManagerUrl = flinkCluster.getJobManagerUrl();

        // jar包信息
        val jarId = etlJob.getJarId();
        val jarPackage = jarPackageService.getById(jarId);
        if (jarPackage == null) {
            throw new EtlJobException("Jar 包未找到: " + jarId);
        }
        // jar包是否存在
        val filePath = jarPackage.getFilePath();
        val file = new File(filePath);
        if (!file.exists()) {
            throw new EtlJobException("Jar 包未找到: " + filePath);
        }

        // jar包同步表信息
        val clusterUploadedJarList = clusterUploadedJarService.queryChain()
                .eq(ClusterUploadedJar::getClusterId, clusterId)
                .eq(ClusterUploadedJar::getJarName, jarPackage.getFileName())
                .orderBy(ClusterUploadedJar::getUploaded, false)
                .list();

        // 如果jar包同步表有记录，那么获取最新的一条；如果有多的记录，那么删除
        ClusterUploadedJar clusterUploadedJar = null;
        val size = clusterUploadedJarList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    clusterUploadedJar = clusterUploadedJarList.get(i);
                } else {
                    val deleteClusterUploadedJar = clusterUploadedJarList.get(i);
                    flinkApiProvider.deleteJar(jobManagerUrl, deleteClusterUploadedJar.getJarId());
                    clusterUploadedJarService.removeById(deleteClusterUploadedJar.getId());
                }
            }
        }

        if (clusterUploadedJar == null) {
            // 上传jar包，获取jarId，并且插入到同步表
            val flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath);
            clusterUploadedJar = new ClusterUploadedJar(clusterId, flinkJarId, jarPackage.getFileName(), System.currentTimeMillis());
            clusterUploadedJarService.save(clusterUploadedJar);
        } else {
            // 如果jar包更新时间 大于 flink jar上传时间 + 5分钟，表示jar包需要更新
            val updateTime = LocalDateTimeUtil.toMs(jarPackage.getUpdateTime());
            val uploaded = clusterUploadedJar.getUploaded();
            if (updateTime > uploaded + UPLOADED_OFFSET.toMillis()) {
                // 重新上传jar包到flink，并更新同步表
                flinkApiProvider.deleteJar(jobManagerUrl, clusterUploadedJar.getJarId());
                val flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath);
                clusterUploadedJar.setJarId(flinkJarId);
                clusterUploadedJar.setUploaded(System.currentTimeMillis());
                clusterUploadedJarService.updateById(clusterUploadedJar);
            }
        }

        val flinkJarId = clusterUploadedJar.getJarId();
        val config = etlJob.getConfig();
        String replacedConfig;
        try {
            replacedConfig = jobVariableService.replaceVariable(config);
        } catch (Exception e) {
            throw new EtlJobException(e);
        }
        val flinkJobId = flinkApiProvider.runJob(jobManagerUrl, flinkJarId, jarPackage.getMainClass(), replacedConfig, savepointPath);

        // 插入任务实例表
        val etlJobInstance = new EtlJobInstance(
                flinkJobId,
                clusterId,
                jarPackage.getId(),
                jobId,
                etlJob.getType(),
                FlinkJobStatusEnum.UNKNOWN
        );
        etlJobInstanceService.save(etlJobInstance);
    }

    public void cancelJob(String jobInstanceId) {
        val etlJobInstance = etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getId, jobInstanceId)
                .eq(EtlJobInstance::getStatus, FlinkJobStatusEnum.RUNNING)
                .one();

        if (etlJobInstance == null) {
            throw new EtlJobException("Etl 任务实例未找到: " + jobInstanceId);
        }

        val clusterId = etlJobInstance.getClusterId();
        val flinkCluster = flinkClusterService.queryChain()
                .eq(FlinkCluster::getId, clusterId)
                .one();

        if (flinkCluster == null) {
            throw new EtlJobException("Flink 集群未找到: " + clusterId);
        }

        flinkApiProvider.cancelJob(flinkCluster.getJobManagerUrl(), jobInstanceId);
    }

    public ResponseVO<List<DictionaryVO>> getCheckpointHistory(Long jobId, String instanceId) {
        val etlJob = etlJobService.getById(jobId);
        if (etlJob == null) {
            return ResponseVO.recordNotFoundError("jobId:" + jobId);
        }
        val clusterId = etlJob.getClusterId();
        val flinkCluster = flinkClusterService.getById(clusterId);
        if (flinkCluster == null) {
            return ResponseVO.recordNotFoundError("clusterId:" + clusterId);
        }

        val vos = flinkApiProvider.getCheckpointHistory(flinkCluster.getJobManagerUrl(), instanceId)
                .stream()
                .filter(item -> "COMPLETED".equals(item.getStatus()))
                .map(item -> {
                    val prefix = item.getSavepoint() ? "SP - " : "CP - ";
                    val label = prefix + LocalDateTimeUtil.format(LocalDateTimeUtil.fromMs(item.getTriggerTimestamp()));
                    val value = item.getExternalPath();
                    return new DictionaryVO(label, value);
                })
                .toList();

        return ResponseVO.ok(vos);
    }
}
