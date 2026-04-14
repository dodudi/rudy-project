package com.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberCreateRequest(
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        @Size(min = 6, max = 20, message = "아이디는 6자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Size(min = 12, message = "비밀번호는 12자 이상으로 입력해야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 최소 1개 이상 포함해야 합니다."
        )
        String password,

        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "닉네임은 영문과 숫자만 사용할 수 있습니다.")
        String nickname
) {
}
