package com.yoyakso.comket.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberInfoResponse {
	@JsonProperty("member_id")
	private Long memberId;
	private String email;
	@JsonProperty("full_name")
	private String fullName;
	@JsonProperty("isAdmin")
	private Boolean isAdmin;
}
