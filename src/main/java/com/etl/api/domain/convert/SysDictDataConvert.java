package com.etl.api.domain.convert;

import com.etl.api.domain.entity.SysDictData;
import com.etl.api.domain.form.SysDictDataCreateForm;
import com.etl.api.domain.form.SysDictDataUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysDictDataConvert {

    SysDictDataConvert INSTANCE = Mappers.getMapper(SysDictDataConvert.class);

    @Mapping(target = "id", ignore = true)
    SysDictData convert(SysDictDataCreateForm form);

    SysDictData convert(SysDictDataUpdateForm form);
}
