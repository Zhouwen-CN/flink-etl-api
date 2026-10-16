package com.etl.api.domain.convert;


import com.etl.api.domain.entity.SysPermission;
import com.etl.api.domain.form.SysPermissionCreateForm;
import com.etl.api.domain.form.SysPermissionUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysPermissionConvert {

    SysPermissionConvert INSTANCE = Mappers.getMapper(SysPermissionConvert.class);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    SysPermission convert(SysPermissionCreateForm form);

    @Mapping(target = "type", ignore = true)
    SysPermission convert(SysPermissionUpdateForm form);
}
