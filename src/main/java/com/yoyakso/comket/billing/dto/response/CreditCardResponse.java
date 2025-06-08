package com.yoyakso.comket.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCardResponse {

    private Long id;
    
    // 마스킹된 카드 번호 (앞 6자리와 뒤 4자리만 표시)
    private String maskedCardNumber;
    
    private String cardholderName;
    
    private String expiryDate;
    
    // CVC는 보안상 응답에 포함하지 않음
}