package com.etl.api.domain.convert;

import com.etl.api.domain.entity.JobVariable;
import com.etl.api.domain.form.JobVariableCreateForm;
import com.etl.api.domain.form.JobVariableUpdateForm;
import com.etl.api.domain.vo.JobVariableVO;
import com.etl.api.util.SPELUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = SPELUtil.class)
public interface JobVariableConvert {

    JobVariableConvert INSTANCE = Mappers.getMapper(JobVariableConvert.class);

    @Mapping(target = "realValue", expression = "java(SPELUtil.parseExpression(entity.getValue(),String.class,entity.getValue()))")
    JobVariableVO convert(JobVariable entity);

    @Mapping(target = "id", ignore = true)
    JobVariable convert(JobVariableCreateForm form);

    JobVariable convert(JobVariableUpdateForm form);
}
