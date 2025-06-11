package com.yoyakso.comket.inquiry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryAnswerRequest;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryCreateRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.service.ServiceInquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
public class ServiceInquiryController {
    
    private final ServiceInquiryService serviceInquiryService;
    
    /**
     * 문의 접수 API
     */
    @PostMapping("")
    public ResponseEntity<ServiceInquiryResponse> createInquiry(
            @Valid @RequestBody ServiceInquiryCreateRequest request) {
        ServiceInquiryResponse response = serviceInquiryService.createInquiry(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 문의 목록 조회 API
     */
    @GetMapping("")
    public ResponseEntity<List<ServiceInquiryResponse>> getAllInquiries() {
        List<ServiceInquiryResponse> responses = serviceInquiryService.getAllInquiries();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 문의 상세 조회 API
     */
    @GetMapping("/{inquiry_id}")
    public ResponseEntity<ServiceInquiryResponse> getInquiry(
            @PathVariable("inquiry_id") Long inquiryId) {
        ServiceInquiryResponse response = serviceInquiryService.getInquiry(inquiryId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 문의 답변 및 답변 이메일 전송 API
     */
    @PostMapping("/{inquiry_id}/answer")
    public ResponseEntity<ServiceInquiryResponse> answerInquiry(
            @PathVariable("inquiry_id") Long inquiryId,
            @Valid @RequestBody ServiceInquiryAnswerRequest request) {
        ServiceInquiryResponse response = serviceInquiryService.answerInquiry(inquiryId, request);
        return ResponseEntity.ok(response);
    }
}