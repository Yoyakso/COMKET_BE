package com.yoyakso.comket.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Getter
@Setter
@Entity
public class Member {

	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	private Long id;

	@NotNull
	@Size(min=2, max=50)
	private String name;

	@NotNull
	@Email
	private String email;

	@NotNull
	@Size(min=8)
	private String password;

	@NotNull
	private String phoneNumber;

	private String profileImageUrl;

	@NotNull
	private PositionType positionType;

	@NotNull
	private LoginType loginType;

	public Member(Long id, String name, String email, String password, String phoneNumber, String profileImageUrl, int positionType, int loginType) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.profileImageUrl = profileImageUrl;
		this.positionType = PositionType.fromType(positionType);
		this.loginType = LoginType.fromType(loginType);
	}

	public Member() {

	}
}
