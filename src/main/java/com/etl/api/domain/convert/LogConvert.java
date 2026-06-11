package com.etl.api.domain.convert;

import com.etl.api.domain.entity.HttpExchangeHistory;
import com.etl.api.domain.vo.RequestLogVO;
import com.etl.api.util.IP2RegionUtil;
import com.etl.api.util.LocalDateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = {
        IP2RegionUtil.class,
        LocalDateTimeUtil.class
})
public interface LogConvert {

    LogConvert INSTANCE = Mappers.getMapper(LogConvert.class);

    @Mapping(target = "url", source = "requestUrl")
    @Mapping(target = "ip", source = "requestIp")
    @Mapping(target = "region", expression = "java(IP2RegionUtil.search(entity.getRequestIp()))")
    @Mapping(target = "method", source = "requestMethod")
    @Mapping(target = "status", source = "responseStatus")
    @Mapping(target = "createTime", expression = "java(LocalDateTimeUtil.fromMs(entity.getTimestamp()))")
    RequestLogVO convert(HttpExchangeHistory entity);
}
