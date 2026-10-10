package com.etl.api.service.impl;

import com.etl.api.domain.entity.Dept;
import com.etl.api.domain.form.DeptCreateForm;
import com.etl.api.domain.form.DeptUpdateForm;
import com.etl.api.domain.vo.DeptVO;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.DeptMapper;
import com.etl.api.mapper.UserMapper;
import com.etl.api.service.DeptService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.etl.api.domain.entity.table.UserTableDef.USER;

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
        if (!validateParent(form.getParentId())) {
            return ResponseVO.recordNotFoundError("父部门 id:" + form.getParentId());
        }
        this.save(Dept.builder()
                .name(name)
                .sort(form.getSort() == null ? 0 : form.getSort())
                .parentId(form.getParentId())
                .build());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyDept(DeptUpdateForm form) {
        Long id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyAdminError();
        }
        Dept dept = this.getById(id);
        if (dept == null) {
            return ResponseVO.recordNotFoundError("部门 id:" + id);
        }
        // 不能将自己的子部门设置为父部门
        if (!Objects.equals(id, form.getParentId())
                && isDescendant(id, form.getParentId())) {
            return ResponseVO.error("父部门不能是自己的子部门");
        }
        if (!validateParent(form.getParentId())) {
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
        this.updateById(Dept.builder()
                .id(id)
                .name(form.getName())
                .sort(form.getSort() == null ? 0 : form.getSort())
                .parentId(form.getParentId())
                .build());
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> removeDept(Long id) {
        // 存在子部门不允许删除
        boolean childExists = this.queryChain()
                .eq(Dept::getParentId, id)
                .exists();
        if (childExists) {
            return ResponseVO.error("删除失败，尚有子部门");
        }
        // 存在用户引用不允许删除
        long userCount = userMapper.selectCountByQuery(
                query().where(USER.DEPT_ID.eq(id)));
        if (userCount > 0) {
            return ResponseVO.error("删除失败，部门下尚有用户");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }

    @Override
    public List<DeptVO> listTree() {
        List<Dept> all = this.list();
        // id -> vo
        java.util.Map<Long, DeptVO> voMap = new java.util.HashMap<>();
        for (Dept dept : all) {
            DeptVO vo = new DeptVO();
            vo.setId(dept.getId());
            vo.setParentId(dept.getParentId());
            vo.setName(dept.getName());
            vo.setSort(dept.getSort());
            voMap.put(dept.getId(), vo);
        }

        List<DeptVO> roots = new ArrayList<>();
        for (Dept dept : all) {
            DeptVO vo = voMap.get(dept.getId());
            Long parentId = dept.getParentId();
            if (parentId != null && voMap.containsKey(parentId)) {
                DeptVO parent = voMap.get(parentId);
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(vo);
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
        return parentId == null || this.getById(parentId) != null;
    }

    /**
     * 判断 targetId 是否为 id 的后代
     */
    private boolean isDescendant(Long id, Long targetId) {
        if (targetId == null) {
            return false;
        }
        List<Dept> all = this.list();
        java.util.Map<Long, List<Dept>> childrenMap = new java.util.HashMap<>();
        for (Dept dept : all) {
            if (dept.getParentId() != null) {
                childrenMap.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>())
                        .add(dept);
            }
        }
        HashSet<Long> visited = new HashSet<>();
        ArrayDeque<Long> stack = new ArrayDeque<>();
        stack.push(id);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            if (Objects.equals(current, targetId)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }
            List<Dept> children = childrenMap.getOrDefault(current, Collections.emptyList());
            for (Dept child : children) {
                stack.push(child.getId());
            }
        }
        return false;
    }
}