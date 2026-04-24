package com.etl.api.domain.base;

import com.mybatisflex.annotation.AbstractUpdateListener;

import java.time.LocalDateTime;

public class UpdateListener extends AbstractUpdateListener<BaseEntity> {
    @Override
    public void doUpdate(BaseEntity baseEntity) {
        baseEntity.setUpdateTime(LocalDateTime.now());
    }
}
