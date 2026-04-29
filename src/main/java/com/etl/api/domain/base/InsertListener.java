package com.etl.api.domain.base;

import com.etl.api.util.SaSessionUtil;
import com.mybatisflex.annotation.AbstractInsertListener;

import java.time.LocalDateTime;

public class InsertListener extends AbstractInsertListener<BaseEntity> {

    @Override
    public void doInsert(BaseEntity baseEntity) {
        baseEntity.setCreateUser(SaSessionUtil.getUsername());
        baseEntity.setCreateTime(LocalDateTime.now());
    }
}
