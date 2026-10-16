package com.etl.api.domain.convert;


import com.etl.api.domain.entity.SysUser;
import com.etl.api.domain.form.SysUserCreateForm;
import com.etl.api.domain.form.SysUserUpdateForm;
import com.etl.api.util.AESUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = AESUtil.class)
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", expression = "java(AESUtil.encrypt(userForm.getPassword()))")
    SysUser convert(SysUserCreateForm userForm);

    @Mapping(target = "password", ignore = true)
    SysUser convert(SysUserUpdateForm userForm);
}
