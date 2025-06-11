package com.yoyakso.comket.inquiry.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.inquiry.enums.InquiryType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInquiryResponse {
    
    private Long id;
    
    private String name;
    
    private String email;
    
    @JsonProperty("inquiry_type")
    private InquiryType type;
    
    private String message;
    
    private String answer;
    
    private boolean answered;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}