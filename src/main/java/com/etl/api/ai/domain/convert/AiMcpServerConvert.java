package com.etl.api.ai.domain.convert;

import com.etl.api.ai.domain.entity.AiMcpServer;
import com.etl.api.ai.domain.form.AiMcpServerCreateForm;
import com.etl.api.ai.domain.form.AiMcpServerUpdateForm;
import com.etl.api.ai.domain.vo.AiMcpServerVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AiMcpServerConvert {

    AiMcpServerConvert INSTANCE = Mappers.getMapper(AiMcpServerConvert.class);

    @Mapping(target = "id", ignore = true)
    AiMcpServer convert(AiMcpServerCreateForm form);

    AiMcpServer convert(AiMcpServerUpdateForm form);
}
