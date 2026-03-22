package com.commerce.ui.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberCreateRequest {

    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 6, max = 20, message = "아이디는 6~20자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문, 숫자만 가능합니다")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 12, message = "비밀번호는 최소 12자여야 합니다")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
            message = "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*)를 각각 1개 이상 포함해야 합니다")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "닉네임은 영문, 숫자만 가능합니다")
    private String nickname;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
