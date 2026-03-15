package com.commerce.service;

import com.commerce.domain.Member;
import com.commerce.dto.MemberCreateRequest;
import com.commerce.dto.MemberFilterRequest;
import com.commerce.dto.MemberResponse;
import com.commerce.repository.MemberRepository;
import com.commerce.repository.MemberSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("생성할 수 없는 아이디 입니다");
        }

        Member member = Member.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .build();

        member = memberRepository.save(member);
        return MemberResponse.from(member);
    }

    public List<MemberResponse> getMembers(MemberFilterRequest filter) {
        return memberRepository.findAll(MemberSpecification.withFilters(filter)).stream()
                .map(MemberResponse::from)
                .toList();
    }
}
