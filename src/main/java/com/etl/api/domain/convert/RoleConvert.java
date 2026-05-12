package com.etl.api.domain.convert;


import com.etl.api.domain.entity.Role;
import com.etl.api.domain.form.RoleCreateForm;
import com.etl.api.domain.form.RoleUpdateForm;
import com.etl.api.domain.vo.DictionaryVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE = Mappers.getMapper(RoleConvert.class);


    @Mapping(target = "id", ignore = true)
    Role convert(RoleCreateForm form);

    Role convert(RoleUpdateForm form);

    @Mapping(target = "value", source = "id")
    @Mapping(target = "label", source = "name")
    DictionaryVO convert(Role role);
}
