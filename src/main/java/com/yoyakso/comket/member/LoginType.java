package com.yoyakso.comket.member;

public enum LoginType {
	GOOGLE(1),
	DIRECT(2);

	private final int type;

	LoginType( int type ){
		this.type = type;
	}
	public int getType(){
		return type;
	}

	public static LoginType fromType(int type) {
		for (LoginType loginType : LoginType.values()) {
			if (loginType.getType() == type) {
				return loginType;
			}
		}
		throw new IllegalArgumentException("Invalid type: " + type);
	}
}
