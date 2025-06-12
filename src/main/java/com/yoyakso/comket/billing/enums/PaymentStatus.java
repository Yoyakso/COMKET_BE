package com.yoyakso.comket.billing.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PaymentStatus {
	IDLE("IDLE"),
	PENDING("PENDING"),
	FAILED("FAILED"),
	PAID("PAID"),
	;
	private final String key;
}
