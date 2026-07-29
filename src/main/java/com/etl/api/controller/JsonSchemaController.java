package com.etl.api.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 任务配置，json schema 校验
 */
@RestController
@RequestMapping("/json")
@Tag(name = "Json Schema 控制器")
public class JsonSchemaController {

    @Value("classpath:META-INF/job-config-schema.json")
    private Resource resource;

    @SaIgnore
    @GetMapping("/schema")
    @CrossOrigin(
            originPatterns = "*",
            methods = RequestMethod.GET,
            allowedHeaders = "*"
    )
    public String getJsonSchema() {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Json Schema文件未找到", e);
        }
    }
}
