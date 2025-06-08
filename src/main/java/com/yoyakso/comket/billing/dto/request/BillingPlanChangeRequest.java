package com.yoyakso.comket.billing.dto.request;

import com.yoyakso.comket.billing.enums.BillingPlan;

import jakarta.validation.constraints.NotNull;
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
public class BillingPlanChangeRequest {

    @NotNull(message = "요금제는 필수입니다.")
    private BillingPlan plan;
}