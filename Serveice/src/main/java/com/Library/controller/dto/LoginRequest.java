package com.Library.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "admin|reader", message = "身份只能是用户或者管理员") //只允许是管理员或者是读者
    private String role;

}
