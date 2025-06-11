package com.yoyakso.comket.inquiry.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryAnswerRequest;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryCreateRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.entity.ServiceInquiry;
import com.yoyakso.comket.inquiry.mapper.ServiceInquiryMapper;
import com.yoyakso.comket.inquiry.repository.ServiceInquiryRepository;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceInquiryService {

    private final ServiceInquiryRepository serviceInquiryRepository;
    private final ServiceInquiryMapper serviceInquiryMapper;
    private final EmailService emailService;
    private final MemberService memberService;

    /**
     * 문의 접수
     */
    @Transactional
    public ServiceInquiryResponse createInquiry(ServiceInquiryCreateRequest request) {
        ServiceInquiry inquiry = serviceInquiryMapper.toEntity(request);
        ServiceInquiry savedInquiry = serviceInquiryRepository.save(inquiry);
        return serviceInquiryMapper.toResponse(savedInquiry);
    }

    /**
     * 문의 목록 조회 - 관리자만 접근 가능
     */
    @Transactional
    public List<ServiceInquiryResponse> getAllInquiries() {
        // 현재 인증된 사용자 가져오기
        Member member = memberService.getAuthenticatedMember();

        // 관리자 권한 확인
        if (!member.getIsAdmin()) {
            throw new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다.");
        }

        List<ServiceInquiry> inquiries = serviceInquiryRepository.findAll();
        return inquiries.stream()
                .map(serviceInquiryMapper::toResponse)
                .toList();
    }

    /**
     * 문의 상세 조회 - 관리자만 접근 가능
     */
    @Transactional
    public ServiceInquiryResponse getInquiry(Long inquiryId) {
        // 현재 인증된 사용자 가져오기
        Member member = memberService.getAuthenticatedMember();

        // 관리자 권한 확인
        if (!member.getIsAdmin()) {
            throw new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다.");
        }

        ServiceInquiry inquiry = getServiceInquiryById(inquiryId);
        return serviceInquiryMapper.toResponse(inquiry);
    }

    /**
     * 문의 답변 및 이메일 전송 - 관리자만 접근 가능
     */
    @Transactional
    public ServiceInquiryResponse answerInquiry(Long inquiryId, ServiceInquiryAnswerRequest request) {
        // 현재 인증된 사용자 가져오기
        Member member = memberService.getAuthenticatedMember();

        // 관리자 권한 확인
        if (!member.getIsAdmin()) {
            throw new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다.");
        }

        ServiceInquiry inquiry = getServiceInquiryById(inquiryId);

        // 이미 답변된 문의인지 확인
        if (inquiry.isAnswered()) {
            throw new CustomException("ALREADY_ANSWERED", "이미 답변된 문의입니다.");
        }

        // 답변 설정
        inquiry.setAnswer(request.getAnswer());
        inquiry.setAnswered(true);

        // 저장
        ServiceInquiry savedInquiry = serviceInquiryRepository.save(inquiry);

        // 이메일 전송
        sendAnswerEmail(savedInquiry);

        return serviceInquiryMapper.toResponse(savedInquiry);
    }

    /**
     * 답변 이메일 전송
     */
    private void sendAnswerEmail(ServiceInquiry inquiry) {
        try {
            // EmailService의 sendInquiryAnswerEmail 메서드를 사용하여 이메일 전송
            emailService.sendInquiryAnswerEmail(inquiry);
        } catch (Exception e) {
            throw new CustomException("EMAIL_SEND_ERROR", "답변 이메일 전송에 실패했습니다.");
        }
    }

    /**
     * ID로 문의 조회
     */
    private ServiceInquiry getServiceInquiryById(Long inquiryId) {
        return serviceInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new CustomException("INQUIRY_NOT_FOUND", "문의를 찾을 수 없습니다."));
    }
}
