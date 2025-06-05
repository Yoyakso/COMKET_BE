package com.yoyakso.comket.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.yoyakso.comket.auth.dto.GoogleDetailRequest;
import com.yoyakso.comket.auth.dto.GoogleDetailResponse;
import com.yoyakso.comket.auth.dto.GoogleTokenRequest;
import com.yoyakso.comket.auth.dto.GoogleTokenResponse;
import com.yoyakso.comket.auth.dto.LoginRequest;
import com.yoyakso.comket.auth.dto.LoginResponse;
import com.yoyakso.comket.auth.dto.TokenReissueResponse;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final RestTemplate restTemplate;
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	@Value("${google.oauth2_client_id}")
	private String googleClientId;
	@Value("${google.oauth2_client_secret}")
	private String googleClientSecret;

	public LoginResponse login(LoginRequest loginRequest) {
		Member member = memberService.getMemberByEmailOptional(loginRequest.getEmail())
			.orElseThrow(() -> new CustomException("LOGIN_VALIDATE_FAILED", "로그인 정보가 정확하지 않습니다."));

		if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
			throw new CustomException("LOGIN_VALIDATE_FAILED", "로그인 정보가 정확하지 않습니다.");
		}

		String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());
		refreshTokenService.saveRefreshToken(member.getId().toString(), refreshToken);

		return LoginResponse.builder()
			.memberId(member.getId())
			.name(member.getFullName())
			.email(member.getEmail())
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.loginPlatformInfo("COMKET")
			.build();
	}

	public LoginResponse handleGoogleLogin(String code, String redirectUri) {
		try {
			// 구글 토큰 요청
			GoogleTokenRequest tokenRequest = new GoogleTokenRequest(code, googleClientId, googleClientSecret,
				redirectUri);
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

	public TokenReissueResponse reissueToken(String expiredAccessToken, String refreshToken) {

		String email = jwtTokenProvider.getEmailFromToken(expiredAccessToken);
		Member member = memberService.getMemberByEmail(email);

		String storedRefreshToken = refreshTokenService.getRefreshToken(member.getId().toString())
			.orElseThrow(() -> new CustomException("REFRESH_NOT_FOUND", "RefreshToken이 없습니다."));

		if (!storedRefreshToken.equals(refreshToken)) {
			throw new CustomException("REFRESH_INVALID", "RefreshToken이 일치하지 않습니다.");
		}

		String newAccessToken = jwtTokenProvider.createAccessToken(email);
		String newRefreshToken = jwtTokenProvider.createRefreshToken(email); // 새로 생성 (rotate)
		refreshTokenService.saveRefreshToken(member.getId().toString(), newRefreshToken); // Redis에 덮어쓰기

		return TokenReissueResponse.builder()
			.accessToken(newAccessToken)
			.refreshToken(newRefreshToken)
			.build();
	}

	public void logout(Member member) {
		refreshTokenService.deleteRefreshToken(member.getId().toString());
	}

	// ---private method---

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
