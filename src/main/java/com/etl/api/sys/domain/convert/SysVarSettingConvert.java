package com.etl.api.sys.domain.convert;

import com.etl.api.etl.util.SPELUtil;
import com.etl.api.sys.domain.entity.SysVarSetting;
import com.etl.api.sys.domain.form.SysVarSettingCreateForm;
import com.etl.api.sys.domain.form.SysVarSettingUpdateForm;
import com.etl.api.sys.domain.vo.SysVarSettingVO;
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
