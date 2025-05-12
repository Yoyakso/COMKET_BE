package com.yoyakso.comket.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.exception.CustomException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {
	private final SecretKey secretKey;
	private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 1시간
	private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 30; // 30일

	public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
		assert secret != null;
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(String email) {
		return generateToken(email, ACCESS_TOKEN_EXPIRATION);
	}

	public String createRefreshToken(String email) {
		return generateToken(email, REFRESH_TOKEN_EXPIRATION);
	}

	public String getEmailFromToken(String token) {
		return parseToken(token).getSubject();
	}

	public boolean isTokenExpired(String token) {
		return parseToken(token).getExpiration().before(new Date());
	}

	public String getEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException("UNAUTHORIZED", "인증되지 않은 사용자입니다.");
		}
		return authentication.getName();
	}

	// ---private method---
	private Claims parseToken(String token) {
		try {
			return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
		} catch (JwtException e) {
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	private String generateToken(String email, long expirationTime) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationTime);

		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(secretKey, SignatureAlgorithm.HS256)
			.compact();
	}
}
