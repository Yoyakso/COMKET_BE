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

	public void saveRefreshToken(String memberId, String refreshToken) {
		String key = getRefreshTokenKey(memberId);
		redisTemplate.opsForValue().set(
			key,
			refreshToken,
			Duration.ofDays(REFRESH_TOKEN_EXPIRATION_DAYS)
		);
	}

	public Optional<String> getRefreshToken(String memberId) {
		String key = getRefreshTokenKey(memberId);
		String token = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(token);
	}

	public void deleteRefreshToken(String memberId) {
		String key = getRefreshTokenKey(memberId);
		redisTemplate.delete(key);
	}

	private String getRefreshTokenKey(String memberId) {
		return "refresh_token:" + memberId;
	}
}
