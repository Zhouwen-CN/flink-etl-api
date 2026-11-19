package com.etl.api.etl.service;

import com.etl.api.etl.domain.entity.EtlJarPackage;
import com.etl.api.sys.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

/**
 * jar包管理表 服务层。
 *
 * @author chen
 * @since 2026-05-13
 */
public interface EtlJarPackageService extends IService<EtlJarPackage> {

    ResponseVO<Void> addJar(String name, MultipartFile file);

    byte[] getFileContent(Long id);

    ResponseVO<Void> removeJar(Long id);

    ResponseVO<Void> removeJarBatch(Collection<Long> ids);

    // 重写 getById：原生 selectOneById 是 SELECT *，会带出大字段，这里改走 queryChain 排除 blob，list() 也会排除 大字段
    EtlJarPackage getById(Long id);

    ResponseVO<Void> modifyJar(Long id, String name, MultipartFile file);
}
