package com.etl.api.domain.base;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.annotation.AbstractUpdateListener;

import java.time.LocalDateTime;

public class UpdateListener extends AbstractUpdateListener<BaseEntity> {
    @Override
    public void doUpdate(BaseEntity baseEntity) {
        baseEntity.setUpdateUser(SaSessionUtil.getUsername());
        baseEntity.setUpdateTime(LocalDateTime.now());
    }
}
