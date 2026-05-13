package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.ClusterUploadedJarSync;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.provider.FlinkApiProvider;
import com.etl.api.service.ClusterUploadedJarSyncService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

/**
 * ETL任务表 服务层实现。
 *
 * @author chen
 * @since 2026-05-11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EtlJobServiceImpl extends ServiceImpl<EtlJobMapper, EtlJob> implements EtlJobService {
    private final FlinkClusterService flinkClusterService;
    private final JarPackageService jarPackageService;
    private final ClusterUploadedJarSyncService clusterUploadedJarSyncService;
    private final FlinkApiProvider flinkApiProvider;

    @Override
    public ResponseVO<Void> addEtlJob(EtlJobCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(EtlJob::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }
        val entity = EtlJobConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> runJob(Long id) {
        val etlJob = this.getById(id);
        if (etlJob == null) {
            return ResponseVO.recordNotFoundError("jobId:" + id);
        }

        // flink集群信息
        val clusterId = etlJob.getClusterId();
        val flinkCluster = flinkClusterService.getById(clusterId);
        if (flinkCluster == null) {
            return ResponseVO.recordNotFoundError("flinkClusterId:" + id);
        }
        val jobManagerUrl = flinkCluster.getJobManagerUrl();

        // jar包信息
        val jarPackage = jarPackageService.getById(etlJob.getJarId());
        if (jarPackage == null) {
            return ResponseVO.recordNotFoundError("jarPackageId:" + id);
        }
        val filePath = jarPackage.getFilePath();

        // jar包同步表信息
        String jarId;
        val clusterUploadedJarSync = clusterUploadedJarSyncService.queryChain()
                .eq(ClusterUploadedJarSync::getClusterId, clusterId)
                .eq(ClusterUploadedJarSync::getJarName, jarPackage.getName())
                .one();

        if (clusterUploadedJarSync == null) {
            // 上传jar包，获取jarId，并且插入到同步表
            jarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, null);
            val uploadedJarSync = new ClusterUploadedJarSync(null, clusterId, jarId, jarPackage.getFileName(), System.currentTimeMillis());
            clusterUploadedJarSyncService.save(uploadedJarSync);
        } else {
            // 如果jar包更新时间大于flink jar上传时间，表示jar包需要更新
            val updateTime = jarPackage.getUpdateTime()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            val uploaded = clusterUploadedJarSync.getUploaded();
            if (updateTime > uploaded) {
                // 重新上传jar包到flink，并更新同步表
                jarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, clusterUploadedJarSync.getJarId());
                clusterUploadedJarSync.setJarId(jarId);
                clusterUploadedJarSyncService.updateById(clusterUploadedJarSync);
            }

            jarId = clusterUploadedJarSync.getJarId();
        }

        val jobId = flinkApiProvider.runJob(jobManagerUrl, jarId, jarPackage.getMainClass(), etlJob.getConfig());
        // todo: 插入 job instance
        log.info("jobId:" + jobId);
        return ResponseVO.ok();
    }
}
