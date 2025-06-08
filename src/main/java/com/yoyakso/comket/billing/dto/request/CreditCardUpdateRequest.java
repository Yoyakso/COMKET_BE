package com.yoyakso.comket.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CreditCardUpdateRequest {

    @NotBlank(message = "카드 번호는 필수입니다.")
    @Size(min = 16, max = 16, message = "카드 번호는 16자리여야 합니다.")
    @Pattern(regexp = "^[0-9]{16}$", message = "카드 번호는 숫자만 입력 가능합니다.")
    private String cardNumber;

    @NotBlank(message = "카드 소유자 이름은 필수입니다.")
    @Size(max = 100, message = "카드 소유자 이름은 100자 이내여야 합니다.")
    private String cardholderName;

    @NotBlank(message = "만료일은 필수입니다.")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "만료일은 MM/YY 형식이어야 합니다.")
    private String expiryDate;

    @NotBlank(message = "CVC는 필수입니다.")
    @Size(min = 3, max = 3, message = "CVC는 3자리여야 합니다.")
    @Pattern(regexp = "^[0-9]{3}$", message = "CVC는 숫자만 입력 가능합니다.")
    private String cvc;
}