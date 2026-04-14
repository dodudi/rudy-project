package com.commerce.ui.controller;

import com.commerce.ui.client.ProductClient;
import com.commerce.ui.dto.ProductCreateRequest;
import com.commerce.ui.dto.ProductFilterRequest;
import com.commerce.ui.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductClient productClient;

    public ProductController(ProductClient productClient) {
        this.productClient = productClient;
    }

    @GetMapping
    public String list(@ModelAttribute ProductFilterRequest filter, Model model) {
        try {
            model.addAttribute("products", productClient.getProducts(filter));
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("filter", filter);
        return "product/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("productCreateRequest", new ProductCreateRequest());
        return "product/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("product", productClient.getProduct(id));
            model.addAttribute("updateRequest", new ProductUpdateRequest());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "product/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute ProductUpdateRequest updateRequest,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        try {
            productClient.updateProduct(id, updateRequest);
            redirectAttributes.addFlashAttribute("success", "상품이 수정되었습니다.");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("product", productClient.getProduct(id));
            model.addAttribute("updateRequest", updateRequest);
            return "product/edit";
        }
    }

    @PostMapping
    public String create(@Valid @ModelAttribute ProductCreateRequest productCreateRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "product/form";
        }

        try {
            productClient.createProduct(productCreateRequest);
            redirectAttributes.addFlashAttribute("success", "상품이 등록되었습니다.");
            return "redirect:/products";
        } catch (Exception e) {
            model.addAttribute("error", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "product/form";
        }
    }
}
