package com.etl.api.domain.convert;

import com.etl.api.domain.entity.ScheduleJob;
import com.etl.api.domain.form.ScheduleJobCreateForm;
import com.etl.api.domain.form.ScheduleJobUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScheduleJobConvert {

    ScheduleJobConvert INSTANCE = Mappers.getMapper(ScheduleJobConvert.class);

    @Mapping(target = "jobEnable", ignore = true)
    @Mapping(target = "id", ignore = true)
    ScheduleJob convert(ScheduleJobCreateForm form);

    @Mapping(target = "jobEnable", ignore = true)
    ScheduleJob convert(ScheduleJobUpdateForm form);
}
