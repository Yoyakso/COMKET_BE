package com.yoyakso.comket.auth;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.yoyakso.comket.auth.dto.GoogleDetailResponse;
import com.yoyakso.comket.auth.dto.GoogleTokenResponse;
import com.yoyakso.comket.auth.dto.LoginResponse;
import com.yoyakso.comket.auth.service.AuthService;
import com.yoyakso.comket.member.service.MemberService;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2ServiceTest {

	@InjectMocks
	private AuthService service;

	@Mock
	private MemberService memberService;

	@Mock
	private RestTemplate restTemplate;

	@Test
	void testGetGoogleUserInfo() {
		// given
		String dummyCode = "dummy-code";

		GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse(); // 구글에서 넘겨준 것으로 가장한 가짜 토큰 정보
		mockTokenResponse.setAccess_token("dummy-access-token");

		GoogleDetailResponse mockDetailResponse = new GoogleDetailResponse(); // 구글에서 넘겨준 것으로 가장한 가짜 유저 정보
		mockDetailResponse.setEmail("test@example.com");
		mockDetailResponse.setName("Test User");

		// 최종 로그인 응답 Mock
		LoginResponse mockLoginResponse = LoginResponse.builder()
			.memberId(1L)
			.name("Test User")
			.email("tset@gmail.com")
			.accessToken("mock-jwt-token")
			.loginPlatformInfo("GOOGLE")
			.build();

		// when - 토큰 요청 Mock
		Mockito.when(restTemplate.postForEntity(
			Mockito.anyString(),
			Mockito.any(HttpEntity.class),
			Mockito.eq(GoogleTokenResponse.class)
		)).thenReturn(ResponseEntity.ok(mockTokenResponse));

		// when - 유저 정보 요청 Mock
		Mockito.when(restTemplate.exchange(
			Mockito.anyString(),
			Mockito.eq(HttpMethod.GET),
			Mockito.any(HttpEntity.class),
			Mockito.eq(GoogleDetailResponse.class)
		)).thenReturn(ResponseEntity.ok(mockDetailResponse));

		// when - MemberSerbvice Mock 설정
		Mockito.when(memberService.handleOAuth2Member(mockDetailResponse))
			.thenReturn(mockLoginResponse);

		LoginResponse response = service.handleGoogleLogin(dummyCode, "");

		// 검증
		assertEquals("mock-jwt-token", response.getAccessToken());
		assertEquals("Test User", response.getName());
	}
}
