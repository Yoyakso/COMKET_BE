package com.yoyakso.comket.billing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatus {
	IDLE("idle"),
	PENDING("pending"),
	FAILED("failed"),
	PAID("paid"),
	;
	private final String key;
}
