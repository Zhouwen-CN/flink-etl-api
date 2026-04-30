package com.etl.api.domain.convert;


import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.util.AESUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = AESUtil.class)
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(AESUtil.encrypt(userForm.getPassword()))")
    User convert(UserCreateForm userForm);

    @Mapping(target = "password", ignore = true)
    User convert(UserUpdateForm userForm);
}
