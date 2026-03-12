package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.event.PaymentRequestEvent;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.OrderRepository;
import com.commerce.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderConcurrencyTest {

    @Autowired private OrderService orderService;
    @Autowired private ProductRepository productRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private OrderRepository orderRepository;

    @MockitoBean
    private KafkaTemplate<String, PaymentRequestEvent> kafkaTemplate;

    private Product product;
    private List<Member> members;

    private static final int STOCK = 5;
    private static final int THREAD_COUNT = 10;

    @BeforeEach
    void setUp() {
        // @Transactional 없이 저장 → 즉시 커밋되어 각 스레드에서 조회 가능
        product = productRepository.save(Product.builder()
                .name("한정판 상품")
                .description("재고 5개")
                .price(10000)
                .stock(STOCK)
                .build());

        members = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            members.add(memberRepository.save(Member.builder()
                    .username("member" + i)
                    .nickname("member" + i)
                    .build()));
        }
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 재고_5개_상품에_10명_동시_주문_5개만_성공() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);   // 동시 출발 신호
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT); // 완료 대기
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final Member member = members.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    orderService.createOrder(new OrderCreateRequest(
                            member.getId(),
                            List.of(new OrderCreateRequest.OrderItem(product.getId(), 1))
                    ));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // 동시 출발
        doneLatch.await();      // 모든 스레드 완료 대기
        executor.shutdown();

        Product updated = productRepository.findById(product.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(STOCK);          // 5개만 성공
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - STOCK); // 5개 실패
        assertThat(updated.getStock()).isEqualTo(0);              // 재고 정확히 0
    }

    @Test
    void 재고보다_적은_동시_주문은_모두_성공() throws InterruptedException {
        int orderCount = 3; // 재고(5)보다 적게 주문
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(orderCount);
        ExecutorService executor = Executors.newFixedThreadPool(orderCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < orderCount; i++) {
            final Member member = members.get(i);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderService.createOrder(new OrderCreateRequest(
                            member.getId(),
                            List.of(new OrderCreateRequest.OrderItem(product.getId(), 1))
                    ));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 실패 없어야 함
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        Product updated = productRepository.findById(product.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(orderCount);
        assertThat(updated.getStock()).isEqualTo(STOCK - orderCount); // 5 - 3 = 2
    }
}
