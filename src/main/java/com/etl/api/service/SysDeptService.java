package com.etl.api.service;

import com.etl.api.domain.entity.SysDept;
import com.etl.api.domain.form.SysDeptCreateForm;
import com.etl.api.domain.form.SysDeptUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysDeptVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 部门表 服务层。
 *
 * @author chen
 * @since 2026-10-10
 */
public interface SysDeptService extends IService<SysDept> {

    ResponseVO<Void> addDept(SysDeptCreateForm form);

    ResponseVO<Void> modifyDept(SysDeptUpdateForm form);

    ResponseVO<Void> removeDept(Long id);

    List<SysDeptVO> listTree(String name);

    List<SysDeptVO> treeSelector();

}
