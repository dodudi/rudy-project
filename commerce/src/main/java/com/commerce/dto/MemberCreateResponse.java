package com.commerce.dto;

import com.commerce.domain.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberCreateResponse {

    private final Long id;

    private final String username;

    private final String nickname;

    public static MemberCreateResponse from(Member member) {
        return new MemberCreateResponse(member.getId(), member.getUsername(), member.getNickname());
    }

}
