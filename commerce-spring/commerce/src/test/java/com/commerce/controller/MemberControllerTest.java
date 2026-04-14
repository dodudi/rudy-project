package com.commerce.controller;

import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberResponse;
import com.commerce.dto.MemberUpdateRequest;
import com.commerce.exception.DuplicateException;
import com.commerce.exception.ErrorCode;
import com.commerce.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @Test
    void 회원생성_성공() throws Exception {
        // given
        String username = "aweraaaa";
        String nickname = "testnick";
        String password = "@testpassword123";

        MemberCreateRequest request = new MemberCreateRequest(username, password, nickname);
        MemberResponse response = new MemberResponse(1L, username, nickname);
        given(memberService.createMember(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.nickname").value(nickname));
    }

    @Test
    void 중복아이디_에러발생() throws Exception {
        // given
        String username = "aweraaaa";
        String nickname = "testnick";
        String password = "@testpassword123";

        MemberCreateRequest request = new MemberCreateRequest(username, password, nickname);
        MemberResponse response = new MemberResponse(1L, username, nickname);

        given(memberService.createMember(any()))
                .willReturn(response)
                .willThrow(new DuplicateException(ErrorCode.DUPLICATE_USERNAME));

        // when & then - 첫 번째 요청 성공
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // when & then - 두 번째 요청 중복으로 실패
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_001"))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 아이디입니다"));
    }

    @Test
    void 아이디_유효성_검증실패() throws Exception {
        // given
        String username = "aa";
        String nickname = "testnick";
        String password = "@teaawee12";

        String username2 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String nickname2 = "testnick";
        String password2 = "@teaawetaetawetawetawe123123";

        String username3 = "";
        String nickname3 = "testnick";
        String password3 = "@teaawetaetawetawetawe123123";

        MemberCreateRequest request = new MemberCreateRequest(username, password, nickname);
        MemberCreateRequest request2 = new MemberCreateRequest(username2, password2, nickname2);
        MemberCreateRequest request3 = new MemberCreateRequest(username3, password3, nickname3);

        // when & then - 6자 이상
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // when & then - 20자 이하
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());

        // when & then - 필수값
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 비밀번호_유효성_검증실패() throws Exception {
        // given - password가 12자 미만
        String username = "aweraaaa";
        String nickname = "testnick";
        String password = "@teaawee12";

        String username2 = "aweraaaa";
        String nickname2 = "testnick";
        String password2 = "teaawetaetawetawetawe123123";

        MemberCreateRequest request = new MemberCreateRequest(username, password, nickname);
        MemberCreateRequest request2 = new MemberCreateRequest(username2, password2, nickname2);

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // when & then
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 닉네임_유효성_검증실패() throws Exception {
        // given
        String username = "aweraaaa";
        String nickname = "t";
        String password = "@test123awefawefawefawe";

        String username2 = "aweraaaa";
        String nickname2 = "tawefaewfaewfawefawefawefawefawefawefawefawefawef";
        String password2 = "@test123awefawefawefawe";

        MemberCreateRequest request = new MemberCreateRequest(username, password, nickname);
        MemberCreateRequest request2 = new MemberCreateRequest(username2, password2, nickname2);

        // when & then - 2자 이상
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // when & then - 20자 이하
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 닉네임수정_성공() throws Exception {
        // given
        Long memberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest("asdfawekfljawek");
        MemberResponse response = new MemberResponse(memberId, "aweraaaa", "asdfawekfljawek");
        given(memberService.updateMember(eq(memberId), any())).willReturn(response);

        // when & then
        mockMvc.perform(put("/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.nickname").value("asdfawekfljawek"));
    }

    @Test
    void 닉네임수정_유효성_검증실패() throws Exception {
        // given
        Long memberId = 1L;
        MemberUpdateRequest request = new MemberUpdateRequest("asdfawekfljawekasdfasdfsadfasdfasdfafs");
        MemberUpdateRequest request2 = new MemberUpdateRequest("");
        MemberUpdateRequest request3 = new MemberUpdateRequest("asdfawekflja");

        MemberResponse response = new MemberResponse(memberId, "aweraaaa", "asdfawekflja");
        given(memberService.updateMember(eq(memberId), any())).willReturn(response);
        given(memberService.updateMember(eq(3333L), any())).willThrow(new DuplicateException(ErrorCode.NOTFOUND_USER));

        // when & then
        mockMvc.perform(put("/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // when & then
        mockMvc.perform(put("/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest());

        // when & then
        mockMvc.perform(put("/members/{id}", 3333L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_002"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다"));
    }
}
