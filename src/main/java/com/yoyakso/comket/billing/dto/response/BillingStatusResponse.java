package com.yoyakso.comket.billing.dto.response;

import java.util.Map;

import com.yoyakso.comket.billing.enums.BillingPlan;

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
public class BillingStatusResponse {

	private Long workspaceId;
	private String workspaceName;
	private BillingPlan currentPlan;
	private String currentPlanDisplayName;
	private int memberCount;

	// DB에 저장된 워크스페이스 빌링 설정(멤버 수와 요금제)을 기준으로 계산된 확정 금액
	private int confirmedAmount;

	private boolean hasCreditCard;
	private Map<String, Integer> memberCountHistory;
	private Map<String, Integer> billingAmountHistory;

	// 현재 실제 활성 멤버 수와 그에 따른 요금제를 기준으로 계산된 이번 달 예상 금액
	// 멤버 수가 변경되었지만 아직 빌링 정보가 업데이트되지 않은 경우 confirmedAmount와 다를 수 있음
	private int estimatedAmount;

	// 날짜에 따라 표시할 금액 (20일 이전: 예상 금액, 20일 이후: 확정 금액)
	private int displayAmount;
}
