package com.etl.api.domain.convert;

import com.etl.api.domain.entity.SysVarSetting;
import com.etl.api.domain.form.SysVarSettingCreateForm;
import com.etl.api.domain.form.SysVarSettingUpdateForm;
import com.etl.api.domain.vo.SysVarSettingVO;
import com.etl.api.util.SPELUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = SPELUtil.class)
public interface SysVarSettingConvert {

    SysVarSettingConvert INSTANCE = Mappers.getMapper(SysVarSettingConvert.class);

    @Mapping(target = "realValue", expression = "java(SPELUtil.parseExpression(entity.getValue(),String.class,entity.getValue()))")
    SysVarSettingVO convert(SysVarSetting entity);

    @Mapping(target = "id", ignore = true)
    SysVarSetting convert(SysVarSettingCreateForm form);

    SysVarSetting convert(SysVarSettingUpdateForm form);
}
