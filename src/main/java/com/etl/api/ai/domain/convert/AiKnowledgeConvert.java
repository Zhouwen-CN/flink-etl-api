package com.etl.api.ai.domain.convert;

import com.etl.api.ai.domain.entity.AiKnowledge;
import com.etl.api.ai.domain.form.AiKnowledgeCreateForm;
import com.etl.api.ai.domain.form.AiKnowledgeUpdateForm;
import com.etl.api.sys.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AiKnowledgeConvert {

    AiKnowledgeConvert INSTANCE = Mappers.getMapper(AiKnowledgeConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dimension", ignore = true)
    AiKnowledge convert(AiKnowledgeCreateForm form);

    @Mapping(target = "vectorDbId", ignore = true)
    @Mapping(target = "embedModelId", ignore = true)
    @Mapping(target = "dimension", ignore = true)
    AiKnowledge convert(AiKnowledgeUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(AiKnowledge entity);
}
