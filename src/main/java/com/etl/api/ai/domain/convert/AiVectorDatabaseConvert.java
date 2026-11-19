package com.etl.api.ai.domain.convert;

import com.etl.api.ai.domain.entity.AiVectorDatabase;
import com.etl.api.ai.domain.form.AiVectorDatabaseCreateForm;
import com.etl.api.ai.domain.form.AiVectorDatabaseUpdateForm;
import com.etl.api.ai.domain.vo.AiVectorDatabaseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AiVectorDatabaseConvert {

    AiVectorDatabaseConvert INSTANCE = Mappers.getMapper(AiVectorDatabaseConvert.class);

    @Mapping(target = "id", ignore = true)
    AiVectorDatabase convert(AiVectorDatabaseCreateForm form);

    AiVectorDatabase convert(AiVectorDatabaseUpdateForm form);

    AiVectorDatabaseVO convert(AiVectorDatabase entity);
}
