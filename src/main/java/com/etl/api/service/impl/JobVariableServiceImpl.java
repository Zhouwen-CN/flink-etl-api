package com.etl.api.service.impl;

import com.etl.api.domain.convert.JobVariableConvert;
import com.etl.api.domain.entity.JobVariable;
import com.etl.api.domain.form.JobVariableCreateForm;
import com.etl.api.domain.vo.ResponseVO;
import com.etl.api.mapper.JobVariableMapper;
import com.etl.api.service.JobVariableService;
import com.etl.api.util.SPELUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 任务变量表 服务层实现。
 *
 * @author chen
 * @since 2026-05-28
 */
@Slf4j
@Service
public class JobVariableServiceImpl extends ServiceImpl<JobVariableMapper, JobVariable> implements JobVariableService {
    private static final Pattern pattern = Pattern.compile("\\$\\{(?<variable>\\w+)}");

    @Override
    public String replaceVariable(String config) {
        val variableMap = this.queryChain()
                .eq(JobVariable::getStatus, true)
                .list()
                .stream()
                .collect(Collectors.toMap(JobVariable::getName, (item) -> {
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

        val replaced = StrSubstitutor.replace(config, variableMap);
        log.debug("变量替换后的任务配置: \n{}", replaced);
        return replaced;
    }

    @Override
    public ResponseVO<Void> addJobVar(JobVariableCreateForm form) {
        val name = form.getName();
        val exists = this.queryChain()
                .eq(JobVariable::getName, name)
                .exists();
        if (exists) {
            return ResponseVO.recordExistsError(name);
        }

        val entity = JobVariableConvert.INSTANCE.convert(form);
        this.save(entity);
        return ResponseVO.ok();
    }
}
