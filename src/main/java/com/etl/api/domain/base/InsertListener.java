package com.etl.api.domain.base;

import com.mybatisflex.annotation.AbstractInsertListener;

import java.time.LocalDateTime;

public class InsertListener extends AbstractInsertListener<BaseEntity> {

    @Override
    public void doInsert(BaseEntity baseEntity) {
        baseEntity.setCreateTime(LocalDateTime.now());
        baseEntity.setUpdateTime(LocalDateTime.now());
    }
}
