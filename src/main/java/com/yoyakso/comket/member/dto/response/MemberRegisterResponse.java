package com.yoyakso.comket.member.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberRegisterResponse {
	@JsonProperty("member_id")
	private Long memberId;
	@JsonProperty("email")
	private String email;
	@JsonProperty("full_name")
	private String fullName;
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("profile_file_url")
	private String profileFileUrl;
}
