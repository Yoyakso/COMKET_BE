package com.yoyakso.comket.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

	private final RedisTemplate<String, String> redisTemplate;
	private final long REFRESH_TOKEN_EXPIRATION_DAYS = 30;

	public void saveRefreshToken(String userId, String refreshToken) {
		String key = getRefreshTokenKey(userId);
		redisTemplate.opsForValue().set(
			key,
			refreshToken,
			Duration.ofDays(REFRESH_TOKEN_EXPIRATION_DAYS)
		);
	}

	public Optional<String> getRefreshToken(String userId) {
		String key = getRefreshTokenKey(userId);
		String token = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(token);
	}

	public void deleteRefreshToken(String userId) {
		String key = getRefreshTokenKey(userId);
		redisTemplate.delete(key);
	}

	private String getRefreshTokenKey(String userId) {
		return "refresh_token:" + userId;
	}
}
