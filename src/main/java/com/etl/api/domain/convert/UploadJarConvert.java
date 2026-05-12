package com.etl.api.domain.convert;

import com.etl.api.domain.entity.UploadJar;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UploadJarConvert {

    UploadJarConvert INSTANCE = Mappers.getMapper(UploadJarConvert.class);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(UploadJar uploadJar);
}
