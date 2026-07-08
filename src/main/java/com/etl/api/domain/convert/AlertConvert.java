package com.etl.api.domain.convert;

import com.etl.api.domain.entity.Alert;
import com.etl.api.domain.form.AlertCreateForm;
import com.etl.api.domain.form.AlertUpdateForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AlertConvert {

    AlertConvert INSTANCE = Mappers.getMapper(AlertConvert.class);

    @Mapping(target = "id", ignore = true)
    Alert convert(AlertCreateForm form);

    Alert convert(AlertUpdateForm form);
}
