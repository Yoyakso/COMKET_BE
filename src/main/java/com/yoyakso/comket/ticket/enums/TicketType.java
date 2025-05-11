package com.yoyakso.comket.ticket.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum TicketType {
	// 티켓 유형 : 기획, 버그, 디자인, 개발, 테스트, 문서화, 회의/논의, 기타
	PLANNING("기획"),
	BUG("버그"),
	DESIGN("디자인"),
	DEVELOPMENT("개발"),
	TEST("테스트"),
	DOCUMENTATION("문서화"),
	MEETING("회의/논의"),
	OTHER("기타");

	@JsonValue
	private final String type;

	TicketType(String type) {
		this.type = type;
	}

}
