package com.yoyakso.comket.oauth2;

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

import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import com.yoyakso.comket.oauth2.dto.GoogleLoginResponse;
import com.yoyakso.comket.oauth2.dto.GoogleTokenResponse;
import com.yoyakso.comket.oauth2.service.GoogleOAuth2ServiceImpl;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2ServiceTest {

	@InjectMocks
	private GoogleOAuth2ServiceImpl service;

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
		GoogleLoginResponse mockLoginResponse = new GoogleLoginResponse(
			"mock-jwt-token",
			"Test User",
			"test@test.com"
		);

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

		GoogleLoginResponse response = service.handleGoogleLogin(dummyCode, "");

		// 검증
		assertEquals("mock-jwt-token", response.getAccessToken());
		assertEquals("Test User", response.getName());
	}
}
