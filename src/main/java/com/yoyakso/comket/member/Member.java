package com.yoyakso.comket.member;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	private Long id;

	@NotNull
	@Size(min=2, max=50)
	private String realName;

	@NotNull
	@Email
	private String email;

	@NotNull
	@Size(min=8)
	private String password;

	@NotNull
	@Size(min=2, max=50)
	private String nickname;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	// private PositionType positionType;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	public Member( String realName, String email, String password ) {
		this.realName = realName;
		this.email = email;
		this.password = password;
	}

	public Member() {

	}
}
