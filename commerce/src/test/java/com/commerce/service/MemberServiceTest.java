package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberCreateResponse;
import com.commerce.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 회원생성_성공() {
        // given
        String username = "test";
        String nickname = "test nickname";
        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(username, nickname);

        // when
        MemberCreateResponse member = memberService.createMember(memberCreateRequest);

        // then
        Assertions.assertThat(member.getUsername()).isEqualTo(username);
        Assertions.assertThat(member.getNickname()).isEqualTo(nickname);
    }

    @Test
    void 중복아이디_에러발생() {
        // given
        String username = "test2";
        String nickname = "test nickname2";

        memberRepository.save(Member.builder()
                .username(username)
                .nickname(nickname)
                .build());

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(username, nickname);

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.createMember(memberCreateRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }
}