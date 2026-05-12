package com.etl.api.domain.convert;

import com.etl.api.domain.entity.DictType;
import com.etl.api.domain.form.DictTypeCreateForm;
import com.etl.api.domain.form.DictTypeUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DictTypeConvert {

    DictTypeConvert INSTANCE = Mappers.getMapper(DictTypeConvert.class);

    @Mapping(target = "id", ignore = true)
    DictType convert(DictTypeCreateForm form);

    DictType convert(DictTypeUpdateForm form);
}
