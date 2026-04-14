package com.commerce.controller;

import com.commerce.domain.OrderStatus;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.dto.OrderItemResponse;
import com.commerce.dto.OrderResponse;
import com.commerce.exception.CommerceException;
import com.commerce.exception.ErrorCode;
import com.commerce.exception.InsufficientStockException;
import com.commerce.exception.NotFoundException;
import com.commerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void 주문생성_성공() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(
                new OrderCreateRequest.OrderItem(10L, 2),
                new OrderCreateRequest.OrderItem(20L, 3)
        ));

        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(10L, "radio", 50000, 2, 100000),
                new OrderItemResponse(20L, "phone", 10000, 3, 30000)
        );
        OrderCreateResponse response = new OrderCreateResponse(1L, 1L, OrderStatus.CREATED, 130000, items, Instant.now());

        given(orderService.createOrder(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(130000))
                .andExpect(jsonPath("$.orderItems.length()").value(2))
                .andExpect(jsonPath("$.orderItems[0].productId").value(10L))
                .andExpect(jsonPath("$.orderItems[0].amount").value(100000))
                .andExpect(jsonPath("$.orderItems[1].productId").value(20L))
                .andExpect(jsonPath("$.orderItems[1].amount").value(30000));
    }

    @Test
    void 주문생성_회원아이디_없는경우_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(null, List.of(
                new OrderCreateRequest.OrderItem(10L, 1)
        ));

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("주문하는 회원 인덱스 아이디는 필수 입력 값입니다."));
    }

    @Test
    void 주문생성_상품목록_없는경우_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of());

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("주문 상품은 최소 1개 이상이어야 합니다."));
    }

    @Test
    void 주문생성_구매상품_수량0개_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(
                new OrderCreateRequest.OrderItem(10L, 0)
        ));

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("상품 수량은 1개 이상이어야 합니다."));
    }

    @Test
    void 주문생성_존재하지_않는회원_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(999L, List.of(
                new OrderCreateRequest.OrderItem(10L, 1)
        ));

        given(orderService.createOrder(any()))
                .willThrow(new NotFoundException(ErrorCode.NOTFOUND_USER));

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("USER_002"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다"));
    }

    @Test
    void 주문생성_존재하지_않는상품_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(
                new OrderCreateRequest.OrderItem(999L, 1)
        ));

        given(orderService.createOrder(any()))
                .willThrow(new NotFoundException(ErrorCode.NOTFOUND_PRODUCT));

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRODUCT_002"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다"));
    }

    @Test
    void 주문목록_조회성공() throws Exception {
        // given
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(10L, "radio", 50000, 2, 100000)
        );
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, "test1234", OrderStatus.CREATED, 100000, items, Instant.now())
        );

        given(orderService.getOrders(any())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/orders")
                        .param("memberId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1L))
                .andExpect(jsonPath("$[0].nickname").value("test1234"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].totalAmount").value(100000))
                .andExpect(jsonPath("$[0].orderItems.length()").value(1))
                .andExpect(jsonPath("$[0].orderItems[0].productId").value(10L))
                .andExpect(jsonPath("$[0].orderItems[0].productName").value("radio"));
    }

    @Test
    void 주문목록_상태필터_조회성공() throws Exception {
        // given
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(10L, "radio", 50000, 1, 50000)
        );
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, "test1234", OrderStatus.CREATED, 50000, items, Instant.now())
        );

        given(orderService.getOrders(any())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/orders")
                        .param("memberId", "1")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    void 주문목록_날짜범위_필터조회_성공() throws Exception {
        // given
        List<OrderItemResponse> items = List.of(
                new OrderItemResponse(10L, "radio", 50000, 1, 50000)
        );
        List<OrderResponse> responses = List.of(
                new OrderResponse(1L, "test1234", OrderStatus.CREATED, 50000, items, Instant.now())
        );

        given(orderService.getOrders(any())).willReturn(responses);

        // when & then
        mockMvc.perform(get("/orders")
                        .param("memberId", "1")
                        .param("startDate", "2025-01-01T00:00:00Z")
                        .param("endDate", "2025-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void 주문취소_성공() throws Exception {
        // given
        doNothing().when(orderService).cancelOrder(1L);

        // when & then
        mockMvc.perform(post("/orders/1/cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 주문취소_존재하지않는_주문_에러발생() throws Exception {
        // given
        doThrow(new NotFoundException(ErrorCode.NOTFOUND_ORDER))
                .when(orderService).cancelOrder(999L);

        // when & then
        mockMvc.perform(post("/orders/999/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_001"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다"));
    }

    @Test
    void 주문취소_취소불가상태_에러발생() throws Exception {
        // given
        doThrow(new CommerceException(ErrorCode.INVALID_ORDER_STATUS))
                .when(orderService).cancelOrder(1L);

        // when & then
        mockMvc.perform(post("/orders/1/cancel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_002"))
                .andExpect(jsonPath("$.message").value("취소 가능한 주문 상태가 아닙니다"));
    }

    @Test
    void 주문생성_재고부족_에러발생() throws Exception {
        // given
        OrderCreateRequest request = new OrderCreateRequest(1L, List.of(
                new OrderCreateRequest.OrderItem(10L, 99999)
        ));

        given(orderService.createOrder(any()))
                .willThrow(new InsufficientStockException(ErrorCode.INSUFFICIENT_STOCK));

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PRODUCT_003"))
                .andExpect(jsonPath("$.message").value("재고가 부족합니다"));
    }
}
