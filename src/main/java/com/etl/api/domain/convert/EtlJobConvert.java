package com.etl.api.domain.convert;

import com.etl.api.domain.entity.EtlJob;
import com.etl.api.domain.form.EtlJobCreateForm;
import com.etl.api.domain.form.EtlJobUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlJobConvert {

    EtlJobConvert INSTANCE = Mappers.getMapper(EtlJobConvert.class);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    EtlJob convert(EtlJobCreateForm form);

    @Mapping(target = "status", ignore = true)
    EtlJob convert(EtlJobUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(EtlJob etlJob);
}
