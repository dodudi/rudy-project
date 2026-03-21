package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Order;
import com.commerce.domain.Outbox;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.dto.OrderFilterRequest;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.exception.EmptyException;
import com.commerce.exception.ErrorCode;
import com.commerce.exception.NotFoundException;
import com.commerce.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    private static final String PAYMENT_REQUEST_TOPIC = "payment.request";

    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        if (request.orderItems().isEmpty()) {
            throw new EmptyException(ErrorCode.EMPTY_PRODUCT);
        }

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_USER));

        Order order = Order.builder().member(member).build();

        // 상품 아이디 오름차순 정렬
        List<Long> productIds = request.orderItems().stream()
                .map(OrderCreateRequest.OrderItem::productId)
                .toList();

        // 상품 목록 Lock 조회 후 ID 별 매핑 처리
        Map<Long, Product> productMap = productRepository.findAllWithLockByIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // 재고 차감 처리
        for (OrderCreateRequest.OrderItem item : request.orderItems()) {
            Product product = Optional.ofNullable(productMap.get(item.productId()))
                    .orElseThrow(() -> new NotFoundException(ErrorCode.NOTFOUND_PRODUCT));
            product.decreaseStock(item.quantity());
            order.addOrderItem(product, item.quantity());
        }

        Order saved = orderRepository.save(order);
        publishPaymentRequest(saved);
        return OrderCreateResponse.from(saved);
    }

    private void publishPaymentRequest(Order order) {
        outboxRepository.save(Outbox.create(
                PAYMENT_REQUEST_TOPIC,
                serialize(new PaymentRequestEvent(order.getId(), order.getMember().getId(), order.getTotalAmount()))
        ));
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 직렬화 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderCreateResponse> getOrders(OrderFilterRequest filter) {
        return orderRepository.findAll(OrderSpecification.withFilters(filter)).stream()
                .map(OrderCreateResponse::from)
                .toList();
    }
}
