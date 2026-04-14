package com.commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
        @NotBlank(message = "닉네임은 입력값입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "닉네임은 영문과 숫자만 사용할 수 있습니다.")
        String nickname
) {
}
