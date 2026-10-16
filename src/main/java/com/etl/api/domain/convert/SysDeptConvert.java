package com.etl.api.domain.convert;

import com.etl.api.domain.entity.SysDept;
import com.etl.api.domain.form.SysDeptCreateForm;
import com.etl.api.domain.form.SysDeptUpdateForm;
import com.etl.api.domain.vo.SysDeptVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

@Mapper(imports = ArrayList.class)
public interface SysDeptConvert {

    SysDeptConvert INSTANCE = Mappers.getMapper(SysDeptConvert.class);

    @Mapping(target = "children", expression = "java(new ArrayList<>())")
    SysDeptVO convert(SysDept entity);

    @Mapping(target = "id", ignore = true)
    SysDept convert(SysDeptCreateForm form);

    SysDept convert(SysDeptUpdateForm form);
}
