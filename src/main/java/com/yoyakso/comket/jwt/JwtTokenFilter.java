package com.yoyakso.comket.jwt;

import java.io.IOException;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yoyakso.comket.exception.CustomException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		final String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			final String token = header.split(" ")[1].trim();

			if (jwtTokenProvider.isTokenExpired(token)) {
				// TODO: 로그 - 토큰 만료
				filterChain.doFilter(request, response);
				return;
			}

			String email = jwtTokenProvider.getEmailFromToken(token);
			if (email == null) {
				// TODO: 로그 - 이메일 검증 실패
				filterChain.doFilter(request, response);
				return;
			}

			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				email, null, Collections.emptyList() // 권한이 없다면 비워두기
			);
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);

		} catch (RuntimeException e) {
			// TODO: 로그 - 토큰 검증 실패
			throw new CustomException("TOKEN_VALIDATE_FAILED", "토큰 검증에 실패했습니다.");
		}

		filterChain.doFilter(request, response);
	}
}
