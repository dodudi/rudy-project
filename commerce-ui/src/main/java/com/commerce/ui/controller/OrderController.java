package com.commerce.ui.controller;

import com.commerce.ui.client.MemberClient;
import com.commerce.ui.client.OrderClient;
import com.commerce.ui.client.ProductClient;
import com.commerce.ui.dto.OrderItemRequest;
import com.commerce.ui.dto.OrderRequest;
import com.commerce.ui.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderClient orderClient;
    private final MemberClient memberClient;
    private final ProductClient productClient;

    public OrderController(OrderClient orderClient, MemberClient memberClient, ProductClient productClient) {
        this.orderClient = orderClient;
        this.memberClient = memberClient;
        this.productClient = productClient;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderClient.getOrders());
        return "order/list";
    }

    @GetMapping("/new")
    public String orderForm(Model model) {
        OrderRequest orderRequest = new OrderRequest();
        model.addAttribute("orderRequest", orderRequest);
        model.addAttribute("members", memberClient.getMembers());
        model.addAttribute("products", productClient.getProducts());
        return "order/form";
    }

    @PostMapping
    public String createOrder(@Valid @ModelAttribute OrderRequest orderRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("members", memberClient.getMembers());
            model.addAttribute("products", productClient.getProducts());
            return "order/form";
        }

        try {
            OrderResponse response = orderClient.createOrder(orderRequest);
            redirectAttributes.addFlashAttribute("order", response);
            return "redirect:/payment/checkout";
        } catch (Exception e) {
            model.addAttribute("error", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("members", memberClient.getMembers());
            model.addAttribute("products", productClient.getProducts());
            return "order/form";
        }
    }

    @GetMapping("/success")
    public String success() {
        return "order/success";
    }
}
