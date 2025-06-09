package com.yoyakso.comket.billing.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BillingPlan {
	BASIC("Basic", 1, 5, 0),
	STARTUP("Startup", 6, 20, 7500),
	PROFESSIONAL("Professional", 21, 50, 8500),
	ENTERPRISE("Enterprise", 51, Integer.MAX_VALUE, 9900);

	private final String displayName;
	private final int minMembers;
	private final int maxMembers;
	private final int pricePerMember; // 원화(KRW) 단위

	public static BillingPlan getPlanForMemberCount(int memberCount) {
		for (BillingPlan plan : BillingPlan.values()) {
			if (memberCount >= plan.getMinMembers() && memberCount <= plan.getMaxMembers()) {
				return plan;
			}
		}
		return ENTERPRISE; // 일치하는 항목이 없으면 가장 높은 등급으로 기본 설정
	}
}
