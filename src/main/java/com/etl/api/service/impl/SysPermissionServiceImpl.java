package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysPermissionConvert;
import com.etl.api.domain.entity.SysPermission;
import com.etl.api.domain.form.SysPermissionCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.SysPermissionMapper;
import com.etl.api.service.SysPermissionService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.val;
import org.springframework.stereotype.Service;

/**
 * 权限表 服务层实现。
 *
 * @author chen
 * @since 2026-04-28
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Override
    public ResponseVO<Void> addPermission(SysPermissionCreateForm form) {
        val code = form.getCode();
        val exists = this.queryChain()
                .eq(SysPermission::getCode, code)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(code);
        }

        val entity = SysPermissionConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }
}
