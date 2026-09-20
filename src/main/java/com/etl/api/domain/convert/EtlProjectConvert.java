package com.etl.api.domain.convert;

import com.etl.api.domain.entity.EtlProject;
import com.etl.api.domain.form.EtlProjectCreateForm;
import com.etl.api.domain.form.EtlProjectUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlProjectConvert {

    EtlProjectConvert INSTANCE = Mappers.getMapper(EtlProjectConvert.class);

    @Mapping(target = "id", ignore = true)
    EtlProject convert(EtlProjectCreateForm form);

    EtlProject convert(EtlProjectUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(EtlProject entity);
}
