package com.etl.api.sys.domain.convert;


import com.etl.api.sys.domain.entity.SysUser;
import com.etl.api.sys.domain.form.SysUserCreateForm;
import com.etl.api.sys.domain.form.SysUserUpdateForm;
import com.etl.api.sys.util.AESUtil;
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
