package com.etl.api.service.impl;

import com.etl.api.domain.convert.DeptConvert;
import com.etl.api.domain.entity.Dept;
import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.DeptCreateForm;
import com.etl.api.domain.form.DeptUpdateForm;
import com.etl.api.domain.vo.DeptVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.DeptMapper;
import com.etl.api.mapper.UserMapper;
import com.etl.api.service.DeptService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门表 服务层实现。
 *
 * @author chen
 * @since 2026-10-10
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    private final UserMapper userMapper;

    @Override
    public ResponseVO<Void> addDept(DeptCreateForm form) {
        String name = form.getName();
        boolean exists = this.queryChain()
                .eq(Dept::getName, name)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        if (validateParent(form.getParentId())) {
            return ResponseVO.recordNotFoundError("父部门 id:" + form.getParentId());
        }

        val entity = DeptConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyDept(DeptUpdateForm form) {
        Long id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyRootDeptError();
        }

        Dept dept = this.getById(id);
        if (dept == null) {
            return ResponseVO.recordNotFoundError("部门 id:" + id);
        }

        if (validateParent(form.getParentId())) {
            return ResponseVO.recordNotFoundError("父部门 id:" + form.getParentId());
        }

        // 名称唯一性（排除自身）
        boolean nameExists = this.queryChain()
                .eq(Dept::getName, form.getName())
                .ne(Dept::getId, id)
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError(form.getName());
        }

        val entity = DeptConvert.INSTANCE.convert(form);
        this.updateById(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDept(Long id) {
        if (id == 1L) {
            return ResponseVO.modifyRootDeptError();
        }

        // 存在子部门不允许删除
        boolean childExists = this.queryChain()
                .eq(Dept::getParentId, id)
                .exists();

        if (childExists) {
            return ResponseVO.error("删除失败，尚有部门依赖");
        }

        // 存在用户引用不允许删除
        val user = userMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq(User::getDeptId, id)
        );

        if (user != null) {
            return ResponseVO.error("删除失败，尚有用户依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public List<DeptVO> listTree(String name) {
        // 全量加载，便于回溯祖先和构建树
        List<Dept> all = this.list();
        Map<Long, Dept> deptMap = all.stream()
                .collect(Collectors.toMap(Dept::getId, d -> d));

        Set<Long> selectedIds;
        if (StringUtils.hasText(name)) {
            // 1. 先查出名称命中的节点
            Set<Long> matchedIds = all.stream()
                    .filter(d -> d.getName() != null && d.getName().contains(name.trim()))
                    .map(Dept::getId)
                    .collect(Collectors.toSet());
            // 2. 再沿 parentId 向上回溯，把全部祖先补进来
            selectedIds = new HashSet<>(matchedIds);
            matchedIds.forEach(id -> {
                Long parentId = deptMap.get(id).getParentId();
                while (parentId != null && selectedIds.add(parentId)) {
                    parentId = deptMap.get(parentId).getParentId();
                }
            });
        } else {
            selectedIds = all.stream().map(Dept::getId).collect(Collectors.toSet());
        }

        // id -> vo
        Map<Long, DeptVO> voMap = new HashMap<>();
        for (Dept dept : all) {
            if (selectedIds.contains(dept.getId())) {
                voMap.put(dept.getId(), DeptConvert.INSTANCE.convert(dept));
            }
        }

        List<DeptVO> roots = new ArrayList<>();
        for (Dept dept : all) {
            DeptVO vo = voMap.get(dept.getId());
            if (vo == null) {
                continue;
            }
            Long parentId = dept.getParentId();
            if (parentId != null && voMap.containsKey(parentId)) {
                voMap.get(parentId).getChildren().add(vo);
            } else {
                roots.add(vo);
            }
        }

        // 按 sort 升序排列（含子部门）
        sortTree(roots);
        return roots;
    }

    private void sortTree(List<DeptVO> list) {
        list.sort(Comparator.comparing(DeptVO::getSort,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (DeptVO vo : list) {
            if (vo.getChildren() != null) {
                sortTree(vo.getChildren());
            }
        }
    }

    private boolean validateParent(Long parentId) {
        return parentId != null && this.getById(parentId) == null;
    }
}