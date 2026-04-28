package com.etl.api.domain.convert;


import com.etl.api.domain.entity.User;
import com.etl.api.domain.form.UserCreateForm;
import com.etl.api.domain.form.UserUpdateForm;
import com.etl.api.enumeration.GenderEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {

    // code 转 enum
    default GenderEnum codeToGenderEnum(Integer code) {
        for (GenderEnum gender : GenderEnum.values()) {
            if (gender.getCode().equals(code)) {
                return gender;
            }
        }
        return GenderEnum.UNKNOW;
    }

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(target = "isEnable", ignore = true)
    @Mapping(target = "id", ignore = true)
    User convert(UserCreateForm userForm);

    @Mapping(target = "isEnable", ignore = true)
    User convert(UserUpdateForm userForm);
}
