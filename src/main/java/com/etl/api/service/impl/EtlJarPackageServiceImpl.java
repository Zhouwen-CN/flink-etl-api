package com.etl.api.service.impl;

import com.etl.api.domain.entity.EtlJarPackage;
import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.EtlJarPackageMapper;
import com.etl.api.mapper.EtlJobMapper;
import com.etl.api.service.EtlJarPackageService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarFile;

/**
 * jar包管理表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
@RequiredArgsConstructor
public class EtlJarPackageServiceImpl extends ServiceImpl<EtlJarPackageMapper, EtlJarPackage> implements EtlJarPackageService {

    private final EtlJobMapper jobMapper;
    @Value("${custom.jar-package.location}")
    private String jarPackageLocation;

    private ResponseVO<Void> saveJar(Long id, String name, MultipartFile uploadFile) {
        if (uploadFile == null) {
            return ResponseVO.error("请上传文件");
        }

        val originalFilename = uploadFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".jar")) {
            return ResponseVO.error("文件格式错误，请上传jar文件");
        }

        val dist = new File(jarPackageLocation, uploadFile.getOriginalFilename());
        try {
            uploadFile.transferTo(dist);
        } catch (Exception e) {
            return ResponseVO.error("文件上传失败");
        }

        try (val jarFile = new JarFile(dist)) {
            String mainClass = jarFile.getManifest().getMainAttributes()
                    .getValue("Main-Class");

            if (StringUtils.hasText(mainClass)) {
                val jarPackage = EtlJarPackage.builder()
                        .id(id)
                        .name(name)
                        .fileName(originalFilename)
                        .filePath(dist.getPath())
                        .mainClass(mainClass)
                        .build();
                this.saveOrUpdate(jarPackage);
            }
        } catch (IOException e) {
            return ResponseVO.error("未能获取jar包入口类: " + e.getMessage());
        }
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> addJar(String name, MultipartFile uploadFile) {
        return this.saveJar(null, name, uploadFile);
    }

    @Override
    public ResponseVO<Void> modifyJar(Long id, String name, MultipartFile file) {
        return this.saveJar(id, name, file);
    }

    @Override
    public ResponseVO<Void> removeJar(Long id) {
        val count = jobMapper.selectCountByQuery(
                QueryWrapper.create()
                        .eq(EtlJob::getJarId, id)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJarBatch(Collection<Long> ids) {
        val count = jobMapper.selectCountByQuery(
                QueryWrapper.create()
                        .in(EtlJob::getJarId, ids)
        );
        if (count > 0) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
