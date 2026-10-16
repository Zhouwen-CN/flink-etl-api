package com.etl.api.domain.convert;


import com.etl.api.domain.entity.SysRole;
import com.etl.api.domain.form.SysRoleCreateForm;
import com.etl.api.domain.form.SysRoleUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysRoleConvert {

    SysRoleConvert INSTANCE = Mappers.getMapper(SysRoleConvert.class);


    @Mapping(target = "id", ignore = true)
    SysRole convert(SysRoleCreateForm form);

    SysRole convert(SysRoleUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(SysRole role);
}
