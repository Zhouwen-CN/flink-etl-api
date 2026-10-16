package com.etl.api.domain.convert;

import com.etl.api.domain.entity.EtlScheduledTask;
import com.etl.api.domain.form.EtlScheduledTaskCreateForm;
import com.etl.api.domain.form.EtlScheduledTaskUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlScheduledTaskConvert {

    EtlScheduledTaskConvert INSTANCE = Mappers.getMapper(EtlScheduledTaskConvert.class);

    @Mapping(target = "deptId", ignore = true)
    @Mapping(target = "jobEnable", ignore = true)
    @Mapping(target = "id", ignore = true)
    EtlScheduledTask convert(EtlScheduledTaskCreateForm form);

    @Mapping(target = "deptId", ignore = true)
    @Mapping(target = "jobEnable", ignore = true)
    EtlScheduledTask convert(EtlScheduledTaskUpdateForm form);
}
