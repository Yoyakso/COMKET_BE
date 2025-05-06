package com.yoyakso.comket.member.entity;

import java.time.LocalDateTime;

import com.yoyakso.comket.member.dto.MemberRegisterRequest;

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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Size(min = 2, max = 50)
	private String realName;

	@NotNull
	@Email
	private String email;

	@NotNull
	@Size(min = 8)
	private String password;

	@Column(updatable = false)
	private LocalDateTime createdAt;

	public Member(String realName, String email, String password) {
		this.realName = realName;
		this.email = email;
		this.password = password;
	}

	// private PositionType positionType;

	public Member() {

	}

	public static Member fromRequest(MemberRegisterRequest memberRegisterRequest) {
		Member member = new Member();
		member.setEmail(memberRegisterRequest.getEmail());
		member.setPassword(memberRegisterRequest.getPassword());
		member.setRealName(memberRegisterRequest.getRealName());
		return member;
	}

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
