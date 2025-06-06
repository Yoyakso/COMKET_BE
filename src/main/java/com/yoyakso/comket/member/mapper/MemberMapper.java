package com.yoyakso.comket.member.mapper;

import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.dto.request.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.request.MemberUpdateRequest;
import com.yoyakso.comket.member.dto.response.MemberInfoResponse;
import com.yoyakso.comket.member.dto.response.MemberRegisterResponse;
import com.yoyakso.comket.member.entity.Member;

@Component
public class MemberMapper {

	public Member toEntity(MemberRegisterRequest request) {
		// password,profileFileURL은 service에서 처리하도록 함
		return Member.builder()
			.email(request.getEmail())
			.fullName(request.getFullName())
			.password(request.getPassword()) // 비밀번호는 암호화 후 저장해야 함
			.build();
	}

	public MemberInfoResponse toMemberInfoResponse(Member member) {
		return MemberInfoResponse.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.fullName(member.getFullName())
			.build();
	}

	public MemberRegisterResponse toMemberRegisterResponse(Member member, String accessToken) {
		return MemberRegisterResponse.builder()
			.memberId(member.getId())
			.email(member.getEmail())
			.fullName(member.getFullName())
			.accessToken(accessToken)
			.build();
	}

	public void updateMemberFromRequest(Member member, MemberUpdateRequest request) {
		updateField(member::setFullName, request.getFullName(), false, "INVALID_MEMBER_NAME", "멤버 이름은 null이 될 수 없습니다.");
		updateField(member::setEmail, request.getEmail(), false, "INVALID_MEMBER_EMAIL", "멤버 이메일은 null이 될 수 없습니다.");
	}

	public MemberInfoResponse toMemberResponse(Member member) {
		return MemberInfoResponse.builder()
			.email(member.getEmail())
			.fullName(member.getFullName())
			.build();
	}

	private <T> void updateField(Consumer<T> setter, T value, boolean isNullable, String errorCode,
		String errorMessage) {
		if (value != null) {
			setter.accept(value);
		} else if (!isNullable) {
			throw new CustomException(errorCode, errorMessage);
		} else {
			setter.accept(null);
		}
	}
}
