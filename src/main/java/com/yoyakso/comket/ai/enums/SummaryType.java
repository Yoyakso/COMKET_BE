package com.yoyakso.comket.ai.enums;

public enum SummaryType {

	GENERAL(0), // 기본 AI 요약
	DEVELOPER(1), // 개발자
	PROJECT_MANAGER(2), // PM, 기획자
	DESIGNER(3), // 디자이너
	DATA_ANALYST(4); // 데이터 엔지니어

	private final int type;

	SummaryType(int type) {
		this.type = type;
	}

	public static SummaryType fromType(int type) {
		for (SummaryType positionType : values()) {
			if (positionType.getType() == type) {
				return positionType;
			}
		}
		throw new IllegalArgumentException("Invalid type: " + type);
	}

	public int getType() {
		return type;
	}
}
