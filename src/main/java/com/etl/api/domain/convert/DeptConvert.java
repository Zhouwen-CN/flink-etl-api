package com.etl.api.domain.convert;

import com.etl.api.domain.entity.Dept;
import com.etl.api.domain.form.DeptCreateForm;
import com.etl.api.domain.form.DeptUpdateForm;
import com.etl.api.domain.vo.DeptVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

@Mapper(imports = ArrayList.class)
public interface DeptConvert {

    DeptConvert INSTANCE = Mappers.getMapper(DeptConvert.class);

    @Mapping(target = "children", expression = "java(new ArrayList<>())")
    DeptVO convert(Dept entity);

    @Mapping(target = "id", ignore = true)
    Dept convert(DeptCreateForm form);

    Dept convert(DeptUpdateForm form);
}
