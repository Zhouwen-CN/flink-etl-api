package com.etl.api.domain.vo;

import com.etl.api.enumeration.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户视图")
public class UserVO {

    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户性别")
    private GenderEnum gender;

    @Schema(description = "账号状态")
    private boolean status;
}
