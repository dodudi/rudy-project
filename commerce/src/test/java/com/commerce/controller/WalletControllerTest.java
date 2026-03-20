package com.commerce.controller;

import com.commerce.dto.WalletCreateRequest;
import com.commerce.dto.WalletResponse;
import com.commerce.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    @Test
    void 잔고생성_성공() throws Exception {
        // given
        Long memberId = 1L;
        Long balance = 100000000L;
        String nickname = "testnick";

        WalletCreateRequest request = new WalletCreateRequest(memberId, balance);
        WalletResponse response = new WalletResponse(1L, nickname, balance);
        given(walletService.createWallet(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nickname").value(nickname))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    void 잔고생성_회원아이디_누락() throws Exception {
        // given: memberId가 null인 요청
        WalletCreateRequest request = new WalletCreateRequest(null, 1000L);

        // when & then
        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("회원 아이디는 필수 값입니다"));
    }

    @Test
    void 잔고생성_잔액_음수() throws Exception {
        // given
        WalletCreateRequest request = new WalletCreateRequest(1L, -1L);

        // when & then
        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잔고 금액은 0원 이상 1억 원 이하만 가능합니다"));
    }

    @Test
    void 잔고생성_잔액_한도초과() throws Exception {
        // given: balance가 1억 1원인 요청
        WalletCreateRequest request = new WalletCreateRequest(1L, 100_000_001L);

        // when & then
        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잔고 금액은 0원 이상 1억 원 이하만 가능합니다"));
    }
}