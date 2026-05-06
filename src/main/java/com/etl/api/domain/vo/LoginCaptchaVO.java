package com.etl.api.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登入验证码视图")
public class LoginCaptchaVO {

    @Schema(description = "UUID")
    private String id;

    @Schema(description = "验证码base64")
    private String captchaBase64;
}
