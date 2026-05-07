package com.etl.api.domain.convert;


import com.etl.api.domain.entity.Permission;
import com.etl.api.domain.form.PermissionCreateForm;
import com.etl.api.domain.form.PermissionUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PermissionConvert {

    PermissionConvert INSTANCE = Mappers.getMapper(PermissionConvert.class);

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "id", ignore = true)
    Permission convert(PermissionCreateForm form);

    @Mapping(target = "type", ignore = true)
    Permission convert(PermissionUpdateForm form);
}
