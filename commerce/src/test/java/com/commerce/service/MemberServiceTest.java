package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberFilterRequest;
import com.commerce.dto.MemberResponse;
import com.commerce.exception.DuplicateException;
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
        String password = "@testpassword123";

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(username, password, nickname);

        // when
        MemberResponse member = memberService.createMember(memberCreateRequest);

        // then
        Assertions.assertThat(member.getUsername()).isEqualTo(username);
        Assertions.assertThat(member.getNickname()).isEqualTo(nickname);
    }

    @Test
    void 중복아이디_에러발생() {
        // given
        String username = "test";
        String nickname = "test nickname";
        String password = "@testpassword123";

        memberRepository.save(Member.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .build());

        MemberCreateRequest memberCreateRequest = new MemberCreateRequest(username, password, nickname);

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.createMember(memberCreateRequest))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이미 사용 중인 아이디입니다");
    }

    @Test
    void 아이디_유효성_에러발생() {
        // given
        String username = "testasdfasdfasdfasdfasdfasdf";
        String nickname = "test nickname";
        String password = "@testpassword123";


        // when & then
        Assertions.assertThatThrownBy(() -> Member.builder()
                        .username(username)
                        .password(password)
                        .nickname(nickname)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디가 너무 깁니다");

        // when & then 2
        Assertions.assertThatThrownBy(() -> Member.builder()
                        .password(password)
                        .nickname(nickname)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디는 필수입니다");
    }

    @Test
    void 비밀번호_유효성_에러발생() {
        // given
        String username = "testasdfasdfasdfasdfasdfasdf";
        String nickname = "test nickname";


        // when & then
        Assertions.assertThatThrownBy(() -> Member.builder()
                        .username(username)
                        .nickname(nickname)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호는 필수입니다");
    }

    @Test
    void 닉네임_유효성_에러발생() {
        // given
        String username = "test";
        String nickname = "testnicknameㅁㄴㅇㄻㄴㅇㄻㄴㅇㄻㄴㅇㄹㄴㅇㅁㅁㄴㅇㄹ";
        String password = "@testpassword123";

        // when & then
        Assertions.assertThatThrownBy(() -> Member.builder()
                        .username(username)
                        .password(password)
                        .nickname(nickname)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임이 너무 깁니다");

        // when & then 2
        Assertions.assertThatThrownBy(() -> Member.builder()
                        .username(username)
                        .password(password)
                        .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은 필수입니다");
    }

    @Test
    void 회원이름_필터링_성공() {
        // given
        String username = "test";
        String nickname = "test nickname";
        String password = "@testpassword123";

        memberRepository.save(Member.builder()
                .username(username)
                .password(password)
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
        String password = "@testpassword123";

        memberRepository.save(Member.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .build());

        MemberFilterRequest filter = new MemberFilterRequest(username + "1", nickname + "1");

        // when
        List<MemberResponse> members = memberService.getMembers(filter);

        // then
        Assertions.assertThat(members).hasSize(0);
    }
}