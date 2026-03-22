package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberFilterRequest;
import com.commerce.dto.MemberResponse;
import com.commerce.dto.MemberUpdateRequest;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
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
    void 닉네임_수정_성공() {
        // given
        Member oldMember = memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        String nickname = "update test nickname";
        MemberUpdateRequest updateRequest = new MemberUpdateRequest(nickname);

        // when
        MemberResponse memberResponse = memberService.updateMember(oldMember.getId(), updateRequest);

        // then
        Assertions.assertThat(memberResponse.getNickname()).isEqualTo(updateRequest.nickname());
    }

    @Test
    void 닉네임수정_유효성_에러발생() {
        // given
        Member oldMember = memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        String nickname = "";
        MemberUpdateRequest updateRequest = new MemberUpdateRequest(nickname);

        String nickname2 = "asdfawekfljaweklfjawlkefjklaweflkjlk";
        MemberUpdateRequest updateRequest2 = new MemberUpdateRequest(nickname2);

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.updateMember(oldMember.getId(), updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임은 필수입니다");

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.updateMember(oldMember.getId(), updateRequest2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("닉네임이 너무 깁니다");

        // when & then
        Assertions.assertThatThrownBy(() -> memberService.updateMember(333L, updateRequest2))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다");
    }

    @Test
    void 회원조회_성공() {
        // given
        Member member = memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        MemberFilterRequest request = new MemberFilterRequest(null, null);

        // when
        List<MemberResponse> members = memberService.getMembers(request);

        // then
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.getFirst().getId()).isEqualTo(member.getId());
        Assertions.assertThat(members.getFirst().getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(members.getFirst().getNickname()).isEqualTo(member.getNickname());
    }

    @Test
    void 회원조회_이름필터_성공() {
        // given
        Member member = memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        MemberFilterRequest request = new MemberFilterRequest("test", null);

        // when
        List<MemberResponse> members = memberService.getMembers(request);

        // then
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.getFirst().getId()).isEqualTo(member.getId());
        Assertions.assertThat(members.getFirst().getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(members.getFirst().getNickname()).isEqualTo(member.getNickname());
    }

    @Test
    void 회원조회_없는이름_필터성공() {
        // given
        memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        MemberFilterRequest request = new MemberFilterRequest("teawtawet", null);

        // when
        List<MemberResponse> members = memberService.getMembers(request);

        // then
        Assertions.assertThat(members).hasSize(0);
    }

    @Test
    void 회원조회_닉네임필터_성공() {
        // given
        Member member = memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        MemberFilterRequest request = new MemberFilterRequest(null, "testnickname");

        // when
        List<MemberResponse> members = memberService.getMembers(request);

        // then
        Assertions.assertThat(members).hasSize(1);
        Assertions.assertThat(members.getFirst().getId()).isEqualTo(member.getId());
        Assertions.assertThat(members.getFirst().getUsername()).isEqualTo(member.getUsername());
        Assertions.assertThat(members.getFirst().getNickname()).isEqualTo(member.getNickname());
    }

    @Test
    void 회원조회_없는닉네임_필터성공() {
        // given
        memberRepository.save(Member.builder()
                .username("test")
                .password("@testpassword123")
                .nickname("testnickname")
                .build());

        MemberFilterRequest request = new MemberFilterRequest(null, "akakak");

        // when
        List<MemberResponse> members = memberService.getMembers(request);

        // then
        Assertions.assertThat(members).hasSize(0);
    }
}