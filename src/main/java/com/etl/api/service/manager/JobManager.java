package com.etl.api.service.manager;

import com.etl.api.domain.entity.ClusterUploadedJar;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.enumeration.FlinkJobStatusEnum;
import com.etl.api.service.ClusterUploadedJarService;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
import com.etl.api.service.provider.FlinkApiProvider;
import com.etl.api.util.LocalDateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobManager {

    private final EtlJobService etlJobService;
    private final FlinkClusterService flinkClusterService;
    private final JarPackageService jarPackageService;
    private final ClusterUploadedJarService clusterUploadedJarService;
    private final FlinkApiProvider flinkApiProvider;
    private final EtlJobInstanceService etlJobInstanceService;

    public ResponseVO<Void> runJob(Long jobId) {
        val etlJob = etlJobService.getById(jobId);
        if (etlJob == null) {
            return ResponseVO.recordNotFoundError("jobId:" + jobId);
        }

        // flink集群信息
        val clusterId = etlJob.getClusterId();
        val flinkCluster = flinkClusterService.getById(clusterId);
        if (flinkCluster == null) {
            return ResponseVO.recordNotFoundError("flinkClusterId:" + clusterId);
        }
        val jobManagerUrl = flinkCluster.getJobManagerUrl();

        // jar包信息
        val jarId = etlJob.getJarId();
        val jarPackage = jarPackageService.getById(jarId);
        if (jarPackage == null) {
            return ResponseVO.recordNotFoundError("jarPackageId:" + jarId);
        }
        val filePath = jarPackage.getFilePath();

        // jar包同步表信息
        ClusterUploadedJar clusterUploadedJar = clusterUploadedJarService.queryChain()
                .eq(ClusterUploadedJar::getClusterId, clusterId)
                .eq(ClusterUploadedJar::getJarName, jarPackage.getFileName())
                .one();

        if (clusterUploadedJar == null) {
            // 上传jar包，获取jarId，并且插入到同步表
            val flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, null);
            clusterUploadedJar = new ClusterUploadedJar(clusterId, flinkJarId, jarPackage.getFileName(), System.currentTimeMillis());
            clusterUploadedJarService.save(clusterUploadedJar);
        } else {
            // 如果jar包更新时间大于flink jar上传时间，表示jar包需要更新
            val updateTime = LocalDateTimeUtil.toMs(jarPackage.getUpdateTime());
            val uploaded = clusterUploadedJar.getUploaded();
            if (updateTime > uploaded) {
                // 重新上传jar包到flink，并更新同步表
                val flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, clusterUploadedJar.getJarId());
                clusterUploadedJar.setJarId(flinkJarId);
                clusterUploadedJar.setUploaded(System.currentTimeMillis());
                clusterUploadedJarService.updateById(clusterUploadedJar);
            }
        }

        val flinkJarId = clusterUploadedJar.getJarId();
        val flinkJobId = flinkApiProvider.runJob(jobManagerUrl, flinkJarId, jarPackage.getMainClass(), etlJob.getConfig());

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
        return ResponseVO.ok();
    }
}
