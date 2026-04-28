package com.etl.api.domain.convert;


import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.enumeration.GenderEnum;
import com.etl.api.util.AESUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = AESUtil.class)
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    // code 转 enum
    default GenderEnum codeToGenderEnum(Integer code) {
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }

        return GenderEnum.UNKNOW;
    }

    @Mapping(target = "isEnabled", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(AESUtil.encrypt(userForm.getPassword()))")
    User convert(UserCreateForm userForm);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isEnabled", ignore = true)
    User convert(UserUpdateForm userForm);
}
