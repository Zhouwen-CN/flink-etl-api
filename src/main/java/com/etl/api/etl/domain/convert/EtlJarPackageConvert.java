package com.etl.api.etl.domain.convert;

import com.etl.api.etl.domain.entity.EtlJarPackage;
import com.etl.api.sys.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EtlJarPackageConvert {

    EtlJarPackageConvert INSTANCE = Mappers.getMapper(EtlJarPackageConvert.class);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(EtlJarPackage etlJarPackage);
}
