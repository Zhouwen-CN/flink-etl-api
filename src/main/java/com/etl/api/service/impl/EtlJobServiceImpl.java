package com.etl.api.service.impl;

import com.etl.api.domain.convert.EtlJobConvert;
import com.etl.api.domain.entity.ClusterUploadedJarSync;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.entity.EtlJobInstance;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.provider.FlinkApiProvider;
import com.etl.api.service.ClusterUploadedJarSyncService;
import com.etl.api.service.EtlJobInstanceService;
import com.etl.api.service.EtlJobService;
import com.etl.api.service.FlinkClusterService;
import com.etl.api.service.JarPackageService;
import com.etl.api.util.LocalDateTimeUtil;
import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.Collection;

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
    private final EtlJobInstanceService etlJobInstanceService;

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
    public ResponseVO<Void> runJob(Long jobId) {
        val etlJob = this.getById(jobId);
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
        String flinkJarId;
        val clusterUploadedJarSync = clusterUploadedJarSyncService.queryChain()
                .eq(ClusterUploadedJarSync::getClusterId, clusterId)
                .eq(ClusterUploadedJarSync::getJarName, jarPackage.getFileName())
                .one();

        if (clusterUploadedJarSync == null) {
            // 上传jar包，获取jarId，并且插入到同步表
            flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, null);
            val uploadedJarSync = new ClusterUploadedJarSync(clusterId, flinkJarId, jarPackage.getFileName(), System.currentTimeMillis());
            clusterUploadedJarSyncService.save(uploadedJarSync);
        } else {
            // 如果jar包更新时间大于flink jar上传时间，表示jar包需要更新
            val updateTime = LocalDateTimeUtil.toMs(jarPackage.getUpdateTime());
            val uploaded = clusterUploadedJarSync.getUploaded();
            if (updateTime > uploaded) {
                // 重新上传jar包到flink，并更新同步表
                flinkJarId = flinkApiProvider.uploadJar(jobManagerUrl, filePath, clusterUploadedJarSync.getJarId());
                clusterUploadedJarSync.setJarId(flinkJarId);
                clusterUploadedJarSyncService.updateById(clusterUploadedJarSync);
            }

            flinkJarId = clusterUploadedJarSync.getJarId();
        }

        val flinkJobId = flinkApiProvider.runJob(jobManagerUrl, flinkJarId, jarPackage.getMainClass(), etlJob.getConfig());

        // 插入任务实例表
        val etlJobInstance = new EtlJobInstance(
                flinkJobId,
                clusterId,
                jarPackage.getId(),
                jobId,
                etlJob.getType(),
                SaSessionUtil.getUsername()
        );
        etlJobInstanceService.save(etlJobInstance);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJob(Long id) {
        val exists = etlJobInstanceService.queryChain()
                .eq(EtlJobInstance::getJobId, id)
                .exists();

        if (exists) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJobBatch(Collection<Long> ids) {
        val exists = etlJobInstanceService.queryChain()
                .in(EtlJobInstance::getJobId, ids)
                .exists();

        if (exists) {
            return ResponseVO.error("删除失败，尚有任务实例依赖");
        }
        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
