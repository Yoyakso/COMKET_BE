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

import com.yoyakso.comket.oauth2.dto.GoogleDetailResponse;
import com.yoyakso.comket.oauth2.dto.GoogleTokenResponse;
import com.yoyakso.comket.oauth2.service.GoogleOAuth2ServiceImpl;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2ServiceTest {

	@InjectMocks
	private GoogleOAuth2ServiceImpl service;

	@Mock
	private RestTemplate restTemplate;

	@Test
	void testGenerateGoogleLoginUrl() {
		String url = service.returnGoogleLoginPageUrl();
		assertTrue(url.contains("https://accounts.google.com/o/oauth2/v2/auth"));
	}

	@Test
	void testGetGoogleUserInfo() {
		// given
		String dummyCode = "dummy-code";
		GoogleTokenResponse mockTokenResponse = new GoogleTokenResponse();
		mockTokenResponse.setAccess_token("dummy-access-token");

		GoogleDetailResponse mockDetailResponse = new GoogleDetailResponse();
		mockDetailResponse.setEmail("test@example.com");
		mockDetailResponse.setName("Test User");

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

		// then
		GoogleDetailResponse response = service.getGoogleUserInfo(dummyCode);

		assertEquals("test@example.com", response.getEmail());
		assertEquals("Test User", response.getName());
	}
}
