package com.yoyakso.comket.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInquiryAnswerRequest {
    
    @NotBlank(message = "답변 내용은 필수 입력 항목입니다.")
    private String answer;
}