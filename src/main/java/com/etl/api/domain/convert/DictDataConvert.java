package com.etl.api.domain.convert;

import com.etl.api.domain.entity.DictData;
import com.etl.api.domain.form.DictDataCreateForm;
import com.etl.api.domain.form.DictDataUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DictDataConvert {

    DictDataConvert INSTANCE = Mappers.getMapper(DictDataConvert.class);

    @Mapping(target = "id", ignore = true)
    DictData convert(DictDataCreateForm form);

    DictData convert(DictDataUpdateForm form);
}
