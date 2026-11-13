package com.etl.api.ai.domain.convert;

import com.etl.api.ai.domain.entity.AiModel;
import com.etl.api.ai.domain.form.AiModelCreateForm;
import com.etl.api.ai.domain.form.AiModelUpdateForm;
import com.etl.api.sys.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AiModelConvert {

    AiModelConvert INSTANCE = Mappers.getMapper(AiModelConvert.class);

    @Mapping(target = "id", ignore = true)
    AiModel convert(AiModelCreateForm form);

    AiModel convert(AiModelUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(AiModel entity);
}
