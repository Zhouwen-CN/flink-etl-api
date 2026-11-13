package com.etl.api.sys.domain.convert;

import com.etl.api.sys.domain.entity.SysRequestLog;
import com.etl.api.sys.domain.vo.SysRequestLogVO;
import com.etl.api.sys.util.IP2RegionUtil;
import com.etl.api.sys.util.LocalDateTimeUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(imports = {
        IP2RegionUtil.class,
        LocalDateTimeUtil.class
})
public interface SysLogConvert {

    SysLogConvert INSTANCE = Mappers.getMapper(SysLogConvert.class);

    @Mapping(target = "url", source = "requestUrl")
    @Mapping(target = "ip", source = "requestIp")
    @Mapping(target = "region", expression = "java(IP2RegionUtil.search(entity.getRequestIp()))")
    @Mapping(target = "method", source = "requestMethod")
    @Mapping(target = "status", source = "responseStatus")
    @Mapping(target = "createTime", expression = "java(LocalDateTimeUtil.fromMs(entity.getTimestamp()))")
    SysRequestLogVO convert(SysRequestLog entity);
}
