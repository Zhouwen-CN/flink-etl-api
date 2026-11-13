package com.etl.api.etl.domain.convert;

import com.etl.api.etl.domain.entity.EtlFlinkCluster;
import com.etl.api.etl.domain.form.EtlFlinkClusterCreateForm;
import com.etl.api.etl.domain.form.EtlFlinkClusterUpdateForm;
import com.etl.api.sys.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlFlinkClusterConvert {

    EtlFlinkClusterConvert INSTANCE = Mappers.getMapper(EtlFlinkClusterConvert.class);

    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    EtlFlinkCluster convert(EtlFlinkClusterCreateForm form);

    @Mapping(target = "version", ignore = true)
    EtlFlinkCluster convert(EtlFlinkClusterUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(EtlFlinkCluster etlFlinkCluster);
}
