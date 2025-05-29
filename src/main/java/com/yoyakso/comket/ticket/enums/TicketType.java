package com.yoyakso.comket.ticket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum TicketType {

	// 티켓 Template Type : 기본형, 기능 개발, 기획/정책 제안, 회의/논의, QA 테스트, 이슈/버그 리포트, 데이터 분석
	// 기본형
	DEFAULT("기본형"),
	// 기능 개발
	FEATURE("기능 개발"),
	// 기획/정책 제안
	PLANNING("기획/정책 제안"),
	// 회의/논의
	MEETING("회의/논의"),
	// QA 테스트
	QA("QA 테스트"),
	// 이슈/버그 리포트
	BUG("이슈/버그 리포트"),
	// 데이터 분석
	DATA_ANALYSIS("데이터 분석");

	@JsonValue
	private final String type;

	TicketType(String type) {
		this.type = type;
	}

}
