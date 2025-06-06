package com.yoyakso.comket.auth.service;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.entity.Member;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {

	private final RedisTemplate<String, String> redisTemplate;
	private final long REFRESH_TOKEN_EXPIRATION_DAYS = 30;
	private final JwtTokenProvider jwtTokenProvider;

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

	public ResponseCookie getRefreshTokenCookie(Member member) {
		String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(Duration.ofDays(30))
			.sameSite("Strict")
			.build();

		saveRefreshToken(member.getId().toString(), refreshToken); // Redis에 덮어쓰기

		return refreshTokenCookie;
	}

	private String getRefreshTokenKey(String memberId) {
		return "refresh_token:" + memberId;
	}
}
