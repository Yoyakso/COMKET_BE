package com.yoyakso.comket.oauth2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.MemberService;
import com.yoyakso.comket.oauth2.dto.GoogleDetailRequest;
import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import com.yoyakso.comket.oauth2.dto.GoogleLoginResponse;
import com.yoyakso.comket.oauth2.dto.GoogleTokenRequest;
import com.yoyakso.comket.oauth2.dto.GoogleTokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2ServiceImpl implements OAuth2Service {

	private final RestTemplate restTemplate;
	private final MemberService memberService;
	@Value("${google_oauth2_client_id}")
	private String googleClientId;
	@Value("${google_oauth2_client_secret}")
	private String googleClientSecret;
	@Value("${google.oauth2_redirect_uri}")
	private String googleRedirectUri;

	@Override
	public String returnGoogleLoginPageUrl() {
		return "https://accounts.google.com/o/oauth2/v2/auth?client_id=" + googleClientId
			+ "&redirect_uri=" + googleRedirectUri
			+ "&response_type=code"
			+ "&scope=email%20profile%20openid"
			+ "&access_type=offline";
	}

	@Override
	public GoogleLoginResponse handleGoogleLogin(String code) {
		try {
			// 구글 토큰 요청
			GoogleTokenRequest tokenRequest = new GoogleTokenRequest(code, googleClientId, googleClientSecret,
				googleRedirectUri);
			GoogleTokenResponse googleToken = requestGoogleToken(tokenRequest);

			if (googleToken == null || googleToken.getAccess_token() == null) {
				throw new CustomException("OAUTH2_TOKEN_ERROR", "구글 토큰 발급에 실패했습니다.");
			}

			GoogleDetailRequest detailRequest = new GoogleDetailRequest(googleToken.getAccess_token());
			GoogleDetailResponse detailResponse = requestGoogleUserInfo(detailRequest);

			return memberService.handleOAuth2Member(detailResponse);

		} catch (RestClientException e) {
			throw new CustomException("OAUTH2_COMMUNICATION_ERROR", "구글 서버와 통신 중 오류가 발생했습니다." + e.getMessage());
		}
	}

	// 구글 로그인 토큰 요청
	private GoogleTokenResponse requestGoogleToken(GoogleTokenRequest requestDto) {
		try {
			String tokenUrl = "https://oauth2.googleapis.com/token";

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("code", requestDto.getCode());
			params.add("client_id", requestDto.getClientId());
			params.add("client_secret", requestDto.getClientSecret());
			params.add("redirect_uri", requestDto.getRedirectUri());
			params.add("grant_type", "authorization_code");

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

			ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
				tokenUrl, request, GoogleTokenResponse.class
			);

			return response.getBody();
		} catch (RestClientException e) {
			throw new CustomException("OAUTH2_TOKEN_REQUEST_FAILED", "구글 토큰 요청에 실패했습니다." + e.getMessage());
		}
	}

	// 유저 정보 조회 요청
	private GoogleDetailResponse requestGoogleUserInfo(GoogleDetailRequest requestDto) {
		try {
			String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(requestDto.getAccessToken());

			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<GoogleDetailResponse> response = restTemplate.exchange(
				userInfoUrl, HttpMethod.GET, request, GoogleDetailResponse.class
			);

			return response.getBody();
		} catch (RestClientException e) {
			throw new CustomException("OAUTH2_USERINFO_REQUEST_FAILED", "구글 유저 정보 요청에 실패했습니다." + e.getMessage());
		}
	}
}
