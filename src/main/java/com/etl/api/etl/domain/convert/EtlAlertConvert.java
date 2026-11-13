package com.etl.api.etl.domain.convert;

import com.etl.api.etl.domain.entity.EtlAlert;
import com.etl.api.etl.domain.form.EtlAlertCreateForm;
import com.etl.api.etl.domain.form.EtlAlertUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlAlertConvert {

    EtlAlertConvert INSTANCE = Mappers.getMapper(EtlAlertConvert.class);

    @Mapping(target = "id", ignore = true)
    EtlAlert convert(EtlAlertCreateForm form);

    EtlAlert convert(EtlAlertUpdateForm form);
}
