package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Order;
import com.commerce.domain.OrderItem;
import com.commerce.domain.Outbox;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.dto.OrderCreateResponse;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.OrderRepository;
import com.commerce.repository.OutboxRepository;
import com.commerce.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Retryable(
            retryFor = PessimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        Order order = Order.create(member);

        List<OrderCreateRequest.OrderItem> sortedItems = request.getOrderItems().stream()
                .sorted(Comparator.comparingLong(OrderCreateRequest.OrderItem::productId))
                .toList();

        for (OrderCreateRequest.OrderItem item : sortedItems) {
            Product product = productRepository.findWithLockById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품"));

            product.decreaseStock(item.quantity());
            order.addOrderItem(product, item.quantity());
        }

        Order save = orderRepository.save(order);
        int totalAmount = save.getOrderItems().stream()
                .mapToInt(OrderItem::getAmount)
                .sum();

        outboxRepository.save(Outbox.create(
                "payment.request",
                serialize(new PaymentRequestEvent(save.getId(), member.getId(), totalAmount))
        ));

        return OrderCreateResponse.from(save);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("이벤트 직렬화 실패", e);
        }
    }
}
