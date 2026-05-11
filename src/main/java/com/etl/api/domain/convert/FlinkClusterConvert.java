package com.etl.api.domain.convert;

import com.etl.api.domain.entity.FlinkCluster;
import com.etl.api.domain.form.FlinkClusterCreateForm;
import com.etl.api.domain.form.FlinkClusterUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FlinkClusterConvert {

    FlinkClusterConvert INSTANCE = Mappers.getMapper(FlinkClusterConvert.class);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    FlinkCluster convert(FlinkClusterCreateForm form);

    @Mapping(target = "version", ignore = true)
    FlinkCluster convert(FlinkClusterUpdateForm form);
}
