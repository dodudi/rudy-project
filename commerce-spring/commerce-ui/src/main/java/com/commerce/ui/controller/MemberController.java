package com.commerce.ui.controller;

import com.commerce.ui.client.MemberClient;
import com.commerce.ui.dto.MemberCreateRequest;
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
@RequestMapping("/members")
public class MemberController {

    private final MemberClient memberClient;

    public MemberController(MemberClient memberClient) {
        this.memberClient = memberClient;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberClient.getMembers());
        return "member/list";
    }

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("memberCreateRequest", new MemberCreateRequest());
        return "member/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute MemberCreateRequest memberCreateRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/form";
        }

        try {
            memberClient.createMember(memberCreateRequest);
            redirectAttributes.addFlashAttribute("success", "회원이 등록되었습니다.");
            return "redirect:/members";
        } catch (Exception e) {
            model.addAttribute("error", "회원 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "member/form";
        }
    }
}
