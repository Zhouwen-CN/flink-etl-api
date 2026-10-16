package com.etl.api.domain.convert;

import com.etl.api.domain.entity.EtlJarPackage;
import com.etl.api.domain.vo.DictionaryVO;
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
