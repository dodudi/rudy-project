package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberCreateResponse;
import com.commerce.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("생성할 수 없는 아이디 입니다");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .build();

        member = memberRepository.save(member);
        return MemberCreateResponse.from(member);
    }
}
