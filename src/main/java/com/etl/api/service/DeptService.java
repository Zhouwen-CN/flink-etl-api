package com.etl.api.service;

import com.etl.api.domain.entity.Dept;
import com.etl.api.domain.form.DeptCreateForm;
import com.etl.api.domain.form.DeptUpdateForm;
import com.etl.api.domain.vo.DeptVO;
import com.etl.api.domain.vo.ResponseVO;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 部门表 服务层。
 *
 * @author chen
 * @since 2026-10-10
 */
public interface DeptService extends IService<Dept> {

    ResponseVO<Void> addDept(DeptCreateForm form);

    ResponseVO<Void> modifyDept(DeptUpdateForm form);

    ResponseVO<Void> removeDept(Long id);

    List<DeptVO> listTree(String name);

    List<DeptVO> treeSelector();

}