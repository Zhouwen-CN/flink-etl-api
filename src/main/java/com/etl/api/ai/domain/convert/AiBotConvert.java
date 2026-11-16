package com.etl.api.ai.domain.convert;

import com.etl.api.ai.domain.entity.AiBot;
import com.etl.api.ai.domain.form.AiBotCreateForm;
import com.etl.api.ai.domain.form.AiBotUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AiBotConvert {

    AiBotConvert INSTANCE = Mappers.getMapper(AiBotConvert.class);

    @Mapping(target = "id", ignore = true)
    AiBot convert(AiBotCreateForm form);

    AiBot convert(AiBotUpdateForm form);
}
