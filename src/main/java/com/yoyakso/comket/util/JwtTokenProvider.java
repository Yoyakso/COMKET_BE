package com.yoyakso.comket.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {
	private final SecretKey secretKey;
	private final long expirationTime = 1000 * 60 * 60 * 24; // 1일

	public JwtTokenProvider(@Value("${JWT_SECRET}") String secret) {
		assert secret != null;
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String createToken(String email) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expirationTime);

		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(now)
			.setExpiration(expiryDate)
			.signWith(secretKey)
			.compact();
	}

	public Claims parseToken(String token) {
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

	public String getTokenFromHeader(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			return authHeader.substring(7);
		}
		return null;
	}

	public String getEmailFromToken(String token) {
		Claims claims = parseToken(token);
		return claims.getSubject();
	}

	public boolean isTokenExpired(String token) {
		Claims claims = parseToken(token);
		return claims.getExpiration().before(new Date());
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = parseToken(token);
			return !isTokenExpired(token) && claims.getSubject() != null;
		} catch (JwtException e) {
			return false;
		}
	}

	public String getSecretKey() {
		return secretKey.getEncoded().toString();
	}
	// public String getSecretKeyAsString() {
	// 	return new String(secretKey.getEncoded(), StandardCharsets.UTF_8);
	// }
	// public String getSecretKeyAsHex() {
	// 	StringBuilder hexString = new StringBuilder();
	// 	for (byte b : secretKey.getEncoded()) {
	// 		hexString.append(String.format("%02x", b));
	// 	}
	// 	return hexString.toString();
	// }
	// public String getSecretKeyAsBase64() {
	// 	return java.util.Base64.getEncoder().encodeToString(secretKey.getEncoded());
	// }
	//

}
