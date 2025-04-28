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
import org.springframework.web.client.RestTemplate;

import com.yoyakso.comket.oauth2.dto.GoogleDetailRequest;
import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import com.yoyakso.comket.oauth2.dto.GoogleTokenRequest;
import com.yoyakso.comket.oauth2.dto.GoogleTokenResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleOAuth2ServiceImpl implements OAuth2Service {

	private final RestTemplate restTemplate;
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
	public GoogleDetailResponse getGoogleUserInfo(String code) {
		GoogleTokenRequest tokenRequest = new GoogleTokenRequest(code, googleClientId, googleClientSecret,
			googleRedirectUri);
		GoogleTokenResponse googleToken = requestGoogleToken(tokenRequest);

		GoogleDetailRequest detailRequest = new GoogleDetailRequest(googleToken.getAccess_token());

		//TODO - 유저 검증 및 토큰 발급 로직 연결

		return requestGoogleUserInfo(detailRequest);
	}

	// 구글 로그인 토큰 요청
	private GoogleTokenResponse requestGoogleToken(GoogleTokenRequest requestDto) {
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
	}

	// 유저 정보 조회 요청
	private GoogleDetailResponse requestGoogleUserInfo(GoogleDetailRequest requestDto) {
		String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(requestDto.getAccessToken());

		HttpEntity<Void> request = new HttpEntity<>(headers);

		ResponseEntity<GoogleDetailResponse> response = restTemplate.exchange(
			userInfoUrl, HttpMethod.GET, request, GoogleDetailResponse.class
		);

		return response.getBody();
	}
}
