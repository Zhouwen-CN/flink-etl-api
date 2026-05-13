package com.etl.api.service.impl;

import com.etl.api.domain.entity.JarPackage;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.JarPackageMapper;
import com.etl.api.service.JarPackageService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import static com.etl.api.domain.entity.table.JarPackageTableDef.JAR_PACKAGE;

/**
 * jar包管理表 服务层实现。
 *
 * @author chen
 * @since 2026-05-13
 */
@Service
public class JarPackageServiceImpl extends ServiceImpl<JarPackageMapper, JarPackage> implements JarPackageService {

    @Value("${custom.jar-package.location}")
    private String jarPackageLocation;

    @Override
    public ResponseVO<Void> addJar(String name, MultipartFile uploadFile) {
        if (uploadFile == null) {
            return ResponseVO.error("未发现文件，请上传文件");
        }

        val originalFilename = uploadFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".jar")) {
            return ResponseVO.error("文件格式错误，请上传jar包文件");
        }

        val dist = new File(jarPackageLocation, uploadFile.getOriginalFilename());
        val path = dist.getPath();
        val exists = this.queryChain()
                .where(JAR_PACKAGE.NAME.eq(name))
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(name + "-" + path);
        }

        try {
            uploadFile.transferTo(dist);
        } catch (Exception e) {
            return ResponseVO.error("文件上传失败");
        }

        try (val jarFile = new JarFile(dist)) {
            String mainClass = jarFile.getManifest().getMainAttributes()
                    .getValue("Main-Class");

            if (StringUtils.hasText(mainClass)) {
                val jarPackage = JarPackage.builder()
                        .name(name)
                        .fileName(originalFilename)
                        .filePath(path)
                        .mainClass(mainClass)
                        .build();

                this.save(jarPackage);
                return ResponseVO.ok();
            }
        } catch (IOException e) {
            // do nothing
        }
        return ResponseVO.error("未获取到jar包入口类");
    }
}
