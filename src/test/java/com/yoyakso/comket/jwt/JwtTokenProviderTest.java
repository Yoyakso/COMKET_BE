package com.yoyakso.comket.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.yoyakso.comket.exception.CustomException;

class JwtTokenProviderTest {

	private final String testSecret = "testSecretKeyWithAtLeast32CharactersForHS256Algorithm";
	private final String testEmail = "test@example.com";
	private JwtTokenProvider jwtTokenProvider;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private Authentication authentication;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create JwtTokenProvider directly without using @InjectMocks
		jwtTokenProvider = new JwtTokenProvider(testSecret);

		// Set expiration times for testing
		ReflectionTestUtils.setField(jwtTokenProvider, "ACCESS_TOKEN_EXPIRATION", 1000 * 60); // 1 minute
		ReflectionTestUtils.setField(jwtTokenProvider, "REFRESH_TOKEN_EXPIRATION", 1000 * 60 * 5); // 5 minutes
	}

	@Test
	void testCreateAccessToken() {
		// Act
		String token = jwtTokenProvider.createAccessToken(testEmail);

		// Assert
		assertNotNull(token);
		assertEquals(testEmail, jwtTokenProvider.getEmailFromToken(token));
		assertFalse(jwtTokenProvider.isTokenExpired(token));
	}

	@Test
	void testCreateRefreshToken() {
		// Act
		String token = jwtTokenProvider.createRefreshToken(testEmail);

		// Assert
		assertNotNull(token);
		assertEquals(testEmail, jwtTokenProvider.getEmailFromToken(token));
		assertFalse(jwtTokenProvider.isTokenExpired(token));
	}

	@Test
	void testGetEmailFromToken() {
		// Arrange
		String token = jwtTokenProvider.createAccessToken(testEmail);

		// Act
		String email = jwtTokenProvider.getEmailFromToken(token);

		// Assert
		assertEquals(testEmail, email);
	}

	@Test
	void testIsTokenExpired_NotExpired() {
		// Arrange
		String token = jwtTokenProvider.createAccessToken(testEmail);

		// Act
		boolean isExpired = jwtTokenProvider.isTokenExpired(token);

		// Assert
		assertFalse(isExpired);
	}

	@Test
	void testGetEmail_Authenticated() {
		// Arrange
		when(authentication.getName()).thenReturn(testEmail);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Act
		String email = jwtTokenProvider.getEmail();

		// Assert
		assertEquals(testEmail, email);
		verify(securityContext).getAuthentication();
		verify(authentication).isAuthenticated();
		verify(authentication).getName();
	}

	@Test
	void testGetEmail_NotAuthenticated() {
		// Arrange
		when(authentication.isAuthenticated()).thenReturn(false);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		// Act & Assert
		CustomException exception = assertThrows(CustomException.class, () -> {
			jwtTokenProvider.getEmail();
		});
		assertEquals("UNAUTHORIZED", exception.getCode());
		verify(securityContext).getAuthentication();
		verify(authentication).isAuthenticated();
	}

	@Test
	void testGetEmail_NoAuthentication() {
		// Arrange
		when(securityContext.getAuthentication()).thenReturn(null);
		SecurityContextHolder.setContext(securityContext);

		// Act & Assert
		CustomException exception = assertThrows(CustomException.class, () -> {
			jwtTokenProvider.getEmail();
		});
		assertEquals("UNAUTHORIZED", exception.getCode());
		verify(securityContext).getAuthentication();
	}
}
