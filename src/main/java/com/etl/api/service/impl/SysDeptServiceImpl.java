package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysDeptConvert;
import com.etl.api.domain.entity.SysDept;
import com.etl.api.domain.entity.SysUser;
import com.etl.api.domain.form.SysDeptCreateForm;
import com.etl.api.domain.form.SysDeptUpdateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.domain.vo.SysDeptVO;
import com.etl.api.mapper.SysDeptMapper;
import com.etl.api.mapper.SysUserMapper;
import com.etl.api.service.SysDeptService;
import com.etl.api.util.SaSessionUtil;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 部门表 服务层实现。
 *
 * @author chen
 * @since 2026-10-10
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysUserMapper userMapper;

    @Override
    public ResponseVO<Void> addDept(SysDeptCreateForm form) {
        String name = form.getName();
        boolean exists = this.queryChain()
                .eq(SysDept::getName, name)
                .exists();

        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        if (validateParent(form.getParentId())) {
            return ResponseVO.recordNotFoundError("父部门 id:" + form.getParentId());
        }

        val entity = SysDeptConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }

    @Override
    public ResponseVO<Void> modifyDept(SysDeptUpdateForm form) {
        Long id = form.getId();
        if (id == 1L) {
            return ResponseVO.modifyRootDeptError();
        }

        SysDept dept = this.getById(id);
        if (dept == null) {
            return ResponseVO.recordNotFoundError("部门 id:" + id);
        }

        if (validateParent(form.getParentId())) {
            return ResponseVO.recordNotFoundError("父部门 id:" + form.getParentId());
        }

        // 名称唯一性（排除自身）
        boolean nameExists = this.queryChain()
                .eq(SysDept::getName, form.getName())
                .ne(SysDept::getId, id)
                .exists();
        if (nameExists) {
            return ResponseVO.recordExistsError(form.getName());
        }

        val entity = SysDeptConvert.INSTANCE.convert(form);
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
                .eq(SysDept::getParentId, id)
                .exists();

        if (childExists) {
            return ResponseVO.error("删除失败，尚有部门依赖");
        }

        // 存在用户引用不允许删除
        val user = userMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq(SysUser::getDeptId, id)
        );

        if (user != null) {
            return ResponseVO.error("删除失败，尚有用户依赖");
        }
        this.removeById(id);
        return ResponseVO.ok();
    }


    private List<SysDeptVO> listTree(Predicate<SysDept> filter, boolean isFilter) {
        // 全量加载，便于回溯祖先和构建树
        List<SysDept> all = this.list();
        Map<Long, SysDept> deptMap = all.stream()
                .collect(Collectors.toMap(SysDept::getId, d -> d));

        Set<Long> selectedIds;
        if (isFilter) {
            // 1. 先查出名称命中的节点
            Set<Long> matchedIds = all.stream()
                    .filter(filter)
                    .map(SysDept::getId)
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
            selectedIds = all.stream().map(SysDept::getId).collect(Collectors.toSet());
        }

        // id -> vo
        Map<Long, SysDeptVO> voMap = new HashMap<>();
        for (SysDept dept : all) {
            if (selectedIds.contains(dept.getId())) {
                voMap.put(dept.getId(), SysDeptConvert.INSTANCE.convert(dept));
            }
        }

        List<SysDeptVO> roots = new ArrayList<>();
        for (SysDept dept : all) {
            SysDeptVO vo = voMap.get(dept.getId());
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

    @Override
    public List<SysDeptVO> listTree(String name) {
        return this.listTree(d -> d.getName().contains(name.trim()), StringUtils.hasText(name));
    }

    @Override
    public List<SysDeptVO> treeSelector() {
        // 如果数据权限范围是所有 或者 传入的 id 为空，则不进行过滤
        val userInfo = SaSessionUtil.getUserInfo();
        val deptId = userInfo.getDeptId();
        val dataScopeAll = userInfo.isDataScopeAll();
        return this.listTree(d -> d.getId().equals(deptId), !dataScopeAll && deptId != null);
    }

    private void sortTree(List<SysDeptVO> list) {
        list.sort(Comparator.comparing(SysDeptVO::getSort,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (SysDeptVO vo : list) {
            if (vo.getChildren() != null) {
                sortTree(vo.getChildren());
            }
        }
    }

    private boolean validateParent(Long parentId) {
        return parentId != null && this.getById(parentId) == null;
    }
}
