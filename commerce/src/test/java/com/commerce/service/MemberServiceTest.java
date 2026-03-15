package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberFilterRequest;
import com.commerce.dto.MemberResponse;
import com.commerce.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

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
        MemberResponse member = memberService.createMember(memberCreateRequest);

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

    @Test
    void 회원이름_필터링_성공() {
        // given
        String username = "test";
        String nickname = "test nickname";

        memberRepository.save(Member.builder()
                .username(username)
                .nickname(nickname)
                .build());

        MemberFilterRequest filter = new MemberFilterRequest(username, nickname);

        // when
        List<MemberResponse> members = memberService.getMembers(filter);

        // then
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.getFirst().getUsername()).isEqualTo(username);
        Assertions.assertThat(members.getFirst().getNickname()).isEqualTo(nickname);
    }

    @Test
    void 회원_없는값_필터링_성공() {
        // given
        String username = "test";
        String nickname = "test nickname";

        memberRepository.save(Member.builder()
                .username(username)
                .nickname(nickname)
                .build());

        MemberFilterRequest filter = new MemberFilterRequest(username + "1", nickname + "1");

        // when
        List<MemberResponse> members = memberService.getMembers(filter);

        // then
        Assertions.assertThat(members).hasSize(0);
    }
}