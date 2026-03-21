package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.domain.Product;
import com.commerce.dto.OrderCreateRequest;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.OrderRepository;
import com.commerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Member member;
    private Product product;


    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
                .username("member")
                .password("@password1234")
                .nickname("member")
                .build()
        );

        Member seller = memberRepository.save(Member.builder()
                .username("seller")
                .password("@password1234")
                .nickname("seller")
                .build());

        product = productRepository.save(Product.builder()
                .name("radio")
                .member(seller)
                .description("radio description")
                .price(50000)
                .stock(1000)
                .build()
        );
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 주문생성_재고감소_동시성_정합성_성공() throws InterruptedException {
        // given
        int threadCount = 1000;
        int orderQuantity = 1;
        int availableStock = product.getStock();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    OrderCreateRequest request = new OrderCreateRequest(member.getId(), List.of(new OrderCreateRequest.OrderItem(product.getId(), orderQuantity)
                    ));
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // when & then
        log.info("successCount: {}, failCount: {}", successCount.get(), failCount.get());
        Product updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
        assertThat(updated.getStock()).isEqualTo(availableStock - orderQuantity * threadCount);
    }

}
