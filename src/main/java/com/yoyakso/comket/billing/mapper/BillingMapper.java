package com.yoyakso.comket.billing.mapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yoyakso.comket.billing.dto.response.BillingStatusResponse;
import com.yoyakso.comket.billing.entity.WorkspacePlan;

@Component
public class BillingMapper {


	/**
	 * Convert WorkspacePlan entity to BillingStatusResponse
	 *
	 * confirmedAmount: 현재 워크스페이스 요금제 설정에 저장된 멤버 수와 요금제를 기준으로 계산된 확정 금액
	 * estimatedAmount: 현재 실제 활성 멤버 수와 그에 따른 요금제를 기준으로 계산된 이번 달 예상 금액
	 *
	 * 차이점:
	 * - confirmedAmount는 DB에 저장된 워크스페이스 요금제 정보를 기반으로 계산됩니다.
	 * - estimatedAmount는 현재 실시간 멤버 수를 반영하여 계산된 실제 예상 금액입니다.
	 * - 멤버 수가 변경되었지만 아직 요금제 정보가 업데이트되지 않은 경우 두 값이 다를 수 있습니다.
	 */
	public BillingStatusResponse toBillingStatusResponse(
		WorkspacePlan workspacePlan,
		Map<String, Integer> memberCountHistory,
		Map<String, Integer> billingAmountHistory,
		int memberCount
	) {
		// 현재 월 정보 가져오기
		YearMonth currentMonth = YearMonth.now();
		String currentMonthStr = currentMonth.toString();

		// 이번달 예상 금액 (현재 월의 빌링 금액)
		int estimatedAmount = billingAmountHistory.getOrDefault(currentMonthStr,
			workspacePlan.calculateMonthlyCost(memberCount));

		// 확정 금액
		int confirmedAmount = workspacePlan.calculateMonthlyCost(memberCount);

		// 현재 날짜 확인
		LocalDate currentDate = LocalDate.now();
		int dayOfMonth = currentDate.getDayOfMonth();

		// 20일 이전이면 예상 금액, 20일 이후면 확정 금액 표시
		int displayAmount = dayOfMonth < 20 ? estimatedAmount : confirmedAmount;

		return BillingStatusResponse.builder()
			.workspaceId(workspacePlan.getWorkspace().getId())
			.workspaceName(workspacePlan.getWorkspace().getName())
			.currentPlan(workspacePlan.getCurrentPlan())
			.currentPlanDisplayName(workspacePlan.getCurrentPlan().getDisplayName())
			.memberCount(memberCount)
			.confirmedAmount(confirmedAmount)
			.hasCreditCard(false) // 신용카드 기능 제거
			.memberCountHistory(memberCountHistory)
			.billingAmountHistory(billingAmountHistory)
			.estimatedAmount(estimatedAmount)
			.displayAmount(displayAmount)
			.build();
	}
}
