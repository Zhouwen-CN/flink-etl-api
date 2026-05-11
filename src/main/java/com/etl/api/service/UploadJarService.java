package com.etl.api.service;

import com.etl.api.domain.entity.UploadJar;
import com.etl.api.domain.form.UploadJarForm;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

/**
 * 上传jar包表 服务层。
 *
 * @author chen
 * @since 2026-05-11
 */
public interface UploadJarService extends IService<UploadJar> {

    ResponseVO<Void> addJar(UploadJarForm form);
}
