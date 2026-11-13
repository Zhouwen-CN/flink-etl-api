package com.etl.api.sys.domain.base;


import com.etl.api.sys.util.SaSessionUtil;
import com.mybatisflex.annotation.AbstractUpdateListener;

import java.time.LocalDateTime;

public class UpdateListener extends AbstractUpdateListener<BaseEntity> {
    @Override
    public void doUpdate(BaseEntity baseEntity) {
        baseEntity.setUpdateUser(SaSessionUtil.getUserInfo().getUsername());
        baseEntity.setUpdateTime(LocalDateTime.now());
    }
}
