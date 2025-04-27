package com.yoyakso.comket.member;

public enum PositionType {

	DEVELOPER(1),
	PROJECT_MANAGER(2),
	DESIGNER(3),
	MARKETING(4),
	DATA_ANALYST(5),
	SYSTEM_ADMINISTRATOR(6);

	private final int type;

	PositionType( int type ){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public static PositionType fromType(int type) {
		for (PositionType positionType : values()) {
			if (positionType.getType() == type) {
				return positionType;
			}
		}
		throw new IllegalArgumentException("Invalid type: " + type);
	}
}
