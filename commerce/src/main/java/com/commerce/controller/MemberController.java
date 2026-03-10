package com.commerce.controller;

import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberCreateResponse;
import com.commerce.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberCreateResponse> createMember(
            @Valid @RequestBody MemberCreateRequest request
    ) {
        MemberCreateResponse member = memberService.createMember(request);
        return ResponseEntity.ok(member);
    }
}
