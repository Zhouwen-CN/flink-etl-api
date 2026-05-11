package com.etl.api.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "上传jar包视图")
public class UploadJarForm {

    @NotBlank
    @Length(max = 30)
    @Schema(description = "jar包名称")
    private String name;

    /**
     * 文件校验放在这里
     */
    @NotNull
    @Schema(description = "jar包文件")
    private MultipartFile file;
}
