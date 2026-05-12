package com.etl.api.domain.base;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.annotation.AbstractInsertListener;
import lombok.val;

import java.time.LocalDateTime;

public class InsertListener extends AbstractInsertListener<BaseEntity> {

    @Override
    public void doInsert(BaseEntity baseEntity) {
        val username = SaSessionUtil.getUsername();
        baseEntity.setCreateUser(username);
        baseEntity.setCreateTime(LocalDateTime.now());
        baseEntity.setUpdateUser(username);
        baseEntity.setUpdateTime(LocalDateTime.now());
    }
}
