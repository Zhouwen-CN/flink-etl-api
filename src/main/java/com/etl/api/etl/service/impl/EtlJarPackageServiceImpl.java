package com.etl.api.etl.service.impl;

import com.etl.api.etl.domain.entity.EtlJarPackage;
import com.etl.api.etl.domain.entity.EtlJob;
import com.etl.api.etl.mapper.EtlJarPackageMapper;
import com.etl.api.etl.service.EtlJarPackageService;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarInputStream;

/**
 * jar包管理表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
@RequiredArgsConstructor
public class EtlJarPackageServiceImpl extends ServiceImpl<EtlJarPackageMapper, EtlJarPackage> implements EtlJarPackageService {

    private ResponseVO<Void> saveJar(Long id, String name, MultipartFile uploadFile) {
        if (uploadFile == null) {
            return ResponseVO.error("请上传文件");
        }

        val originalFilename = uploadFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".jar")) {
            return ResponseVO.error("文件格式错误，请上传jar文件");
        }

        byte[] fileContent;
        String mainClass;
        try {
            fileContent = uploadFile.getBytes();
            try (val jarStream = new JarInputStream(new ByteArrayInputStream(fileContent))) {
                val manifest = jarStream.getManifest();
                mainClass = manifest == null ? null : manifest.getMainAttributes().getValue("Main-Class");
            }
        } catch (IOException e) {
            return ResponseVO.error("未能获取jar包入口类: " + e.getMessage());
        }

        if (!StringUtils.hasText(mainClass)) {
            return ResponseVO.error("未能获取jar包入口类");
        }

        val jarPackage = EtlJarPackage.builder()
                .id(id)
                .name(name)
                .fileName(originalFilename)
                .fileContent(fileContent)
                .mainClass(mainClass)
                .build();
        this.saveOrUpdate(jarPackage);
        return ResponseVO.ok();
    }

    @Override
    public byte[] getFileContent(Long id) {
        val jarPackage = queryChain()
                .select(EtlJarPackage::getFileContent)
                .eq(EtlJarPackage::getId, id)
                .one();
        return jarPackage == null ? null : jarPackage.getFileContent();
    }

    @Override
    public ResponseVO<Void> addJar(String name, MultipartFile uploadFile) {
        return this.saveJar(null, name, uploadFile);
    }

    @Override
    public EtlJarPackage getJarById(Long id) {
        return queryChain()
                .eq(EtlJarPackage::getId, id)
                .one();
    }

    @Override
    public ResponseVO<Void> modifyJar(Long id, String name, MultipartFile file) {
        // 只传name，仅更新名称
        if (file == null) {
            val jarPackage = EtlJarPackage.builder()
                    .id(id)
                    .name(name)
                    .build();
            this.updateById(jarPackage);
            return ResponseVO.ok();
        }
        return this.saveJar(id, name, file);
    }

    @Override
    public ResponseVO<Void> removeJar(Long id) {
        val exists = QueryChain.of(EtlJob.class)
                .eq(EtlJob::getJarId, id)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeJarBatch(Collection<Long> ids) {
        val exists = QueryChain.of(EtlJob.class)
                .in(EtlJob::getJarId, ids)
                .exists();
        if (exists) {
            return ResponseVO.error("删除失败，尚有任务依赖");
        }

        this.removeByIds(ids);
        return ResponseVO.ok();
    }
}
