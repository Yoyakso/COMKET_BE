package com.yoyakso.comket.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yoyakso.comket.admin.service.AdminStatisticsService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryAnswerRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.service.ServiceInquiryService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    
    private final MemberService memberService;
    private final ServiceInquiryService inquiryService;
    private final AdminStatisticsService statisticsService;
    
    /**
     * 관리자 권한 확인 메서드
     */
    private void validateAdminPermission() {
        Member member = memberService.getAuthenticatedMember();
        if (!member.getIsAdmin()) {
            throw new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다.");
        }
    }
    
    /**
     * 관리자 대시보드 페이지
     */
    @GetMapping("")
    public String dashboard(Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 운영 통계 데이터
        model.addAttribute("operationStats", statisticsService.getOperationStatistics());
        
        // 사용자 행동 분석 데이터
        model.addAttribute("userBehaviorStats", statisticsService.getUserBehaviorStatistics());
        
        // 기능 사용률 분석 데이터
        model.addAttribute("featureUsageStats", statisticsService.getFeatureUsageStatistics());
        
        return "admin/dashboard";
    }
    
    /**
     * 문의 목록 페이지
     */
    @GetMapping("/inquiries")
    public String inquiries(Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 문의 목록 조회
        List<ServiceInquiryResponse> inquiries = inquiryService.getAllInquiries();
        model.addAttribute("inquiries", inquiries);
        
        return "admin/inquiries";
    }
    
    /**
     * 문의 상세 페이지
     */
    @GetMapping("/inquiries/{id}")
    public String inquiryDetail(@PathVariable Long id, Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 문의 상세 조회
        ServiceInquiryResponse inquiry = inquiryService.getInquiry(id);
        model.addAttribute("inquiry", inquiry);
        
        return "admin/inquiry-detail";
    }
    
    /**
     * 문의 답변 처리
     */
    @PostMapping("/inquiries/{id}/answer")
    public String answerInquiry(@PathVariable Long id, @RequestParam String answer) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 문의 답변 처리
        ServiceInquiryAnswerRequest request = new ServiceInquiryAnswerRequest();
        request.setAnswer(answer);
        inquiryService.answerInquiry(id, request);
        
        return "redirect:/admin/inquiries";
    }
    
    /**
     * 운영 통계 페이지
     */
    @GetMapping("/statistics/operations")
    public String operationStatistics(Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 운영 통계 데이터
        model.addAttribute("stats", statisticsService.getOperationStatistics());
        
        return "admin/statistics-operations";
    }
    
    /**
     * 사용자 행동 분석 페이지
     */
    @GetMapping("/statistics/user-behavior")
    public String userBehaviorStatistics(Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 사용자 행동 분석 데이터
        model.addAttribute("stats", statisticsService.getUserBehaviorStatistics());
        
        return "admin/statistics-user-behavior";
    }
    
    /**
     * 기능 사용률 분석 페이지
     */
    @GetMapping("/statistics/feature-usage")
    public String featureUsageStatistics(Model model) {
        // 관리자 권한 확인
        validateAdminPermission();
        
        // 기능 사용률 분석 데이터
        model.addAttribute("stats", statisticsService.getFeatureUsageStatistics());
        
        return "admin/statistics-feature-usage";
    }
}