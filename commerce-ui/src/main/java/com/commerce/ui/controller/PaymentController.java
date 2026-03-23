package com.commerce.ui.controller;

import com.commerce.ui.client.PaymentClient;
import com.commerce.ui.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Value("${toss.client-key}")
    private String clientKey;

    private final PaymentClient paymentClient;

    public PaymentController(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        OrderResponse order = (OrderResponse) model.asMap().get("order");
        if (order == null) {
            return "redirect:/orders";
        }
        model.addAttribute("clientKey", clientKey);
        return "payment/checkout";
    }

    @GetMapping("/success")
    public String success(@RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam int amount,
                          Model model) {
        Long parsedOrderId = Long.parseLong(orderId.split("-")[0]);
        paymentClient.confirmPayment(paymentKey, parsedOrderId, orderId, amount);
        model.addAttribute("paymentKey", paymentKey);
        model.addAttribute("orderId", parsedOrderId);
        model.addAttribute("amount", amount);
        return "payment/success";
    }

    @GetMapping("/fail")
    public String fail(Model model) {
        return "payment/fail";
    }
}
