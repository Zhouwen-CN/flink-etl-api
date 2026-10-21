package com.etl.api.service.impl;

import com.etl.api.domain.convert.SysVarSettingConvert;
import com.etl.api.domain.entity.SysVarSetting;
import com.etl.api.domain.form.SysVarSettingCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.SysVarSettingMapper;
import com.etl.api.service.SysVarSettingService;
import com.etl.api.util.SPELUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 变量配置表 服务层实现。
 *
 * @author chen
 * @since 2026-05-28
 */
@Slf4j
@Service
public class SysVarSettingServiceImpl extends ServiceImpl<SysVarSettingMapper, SysVarSetting> implements SysVarSettingService {
    private static final Pattern pattern = Pattern.compile("\\$\\{(?<variable>[^}]+)}");

    @Override
    public String replaceVariable(String config) {
        val variableMap = this.queryChain()
                .eq(SysVarSetting::getStatus, true)
                .list()
                .stream()
                .collect(Collectors.toMap(SysVarSetting::getName, (item) -> {
                            String value = item.getValue();
                            return SPELUtil.parseExpression(value, String.class, value);
                        })
                );

        val matcher = pattern.matcher(config);
        while (matcher.find()) {
            val variable = matcher.group("variable");
            if (!variableMap.containsKey(variable)) {
                throw new RuntimeException("任务替换变量未找到: " + variable);
            }
        }

        return StrSubstitutor.replace(config, variableMap);
    }

    @Override
    public ResponseVO<Void> addJobVar(SysVarSettingCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(SysVarSetting::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = SysVarSettingConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }
}
