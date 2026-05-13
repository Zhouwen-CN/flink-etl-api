package com.etl.api.service;

import com.etl.api.domain.entity.JarPackage;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * jar包管理表 服务层。
 *
 * @author chen
 * @since 2026-05-13
 */
public interface JarPackageService extends IService<JarPackage> {

    ResponseVO<Void> addJar(String name, MultipartFile file);

    ResponseVO<Void> removeJar(Long id);

    ResponseVO<Void> removeJarBatch(Collection<Long> ids);
}
