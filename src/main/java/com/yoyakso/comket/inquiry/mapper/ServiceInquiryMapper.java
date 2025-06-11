package com.yoyakso.comket.inquiry.mapper;

import org.springframework.stereotype.Component;

import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryCreateRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.entity.ServiceInquiry;

@Component
public class ServiceInquiryMapper {

    public ServiceInquiry toEntity(ServiceInquiryCreateRequest request) {
        return ServiceInquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .type(request.getType())
                .message(request.getMessage())
                .build();
    }

    public ServiceInquiryResponse toResponse(ServiceInquiry inquiry) {
        return ServiceInquiryResponse.builder()
                .id(inquiry.getId())
                .name(inquiry.getName())
                .email(inquiry.getEmail())
                .type(inquiry.getType())
                .message(inquiry.getMessage())
                .answer(inquiry.getAnswer())
                .answered(inquiry.isAnswered())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }
}