package com.etl.api.domain.convert;

import com.etl.api.domain.entity.JarPackage;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface JarPackageConvert {

    JarPackageConvert INSTANCE = Mappers.getMapper(JarPackageConvert.class);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(JarPackage jarPackage);
}
