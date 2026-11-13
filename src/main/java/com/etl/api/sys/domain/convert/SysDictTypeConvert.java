package com.etl.api.sys.domain.convert;

import com.etl.api.sys.domain.entity.SysDictType;
import com.etl.api.sys.domain.form.SysDictTypeCreateForm;
import com.etl.api.sys.domain.form.SysDictTypeUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysDictTypeConvert {

    SysDictTypeConvert INSTANCE = Mappers.getMapper(SysDictTypeConvert.class);

    @Mapping(target = "id", ignore = true)
    SysDictType convert(SysDictTypeCreateForm form);

    SysDictType convert(SysDictTypeUpdateForm form);
}
