package com.yoyakso.comket.configuration;

import static org.springframework.security.config.Customizer.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.yoyakso.comket.jwt.JwtTokenFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenFilter jwtTokenFilter;
	private final AdminAuthenticationEntryPoint adminAuthenticationEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health").permitAll()
				.requestMatchers("/api/v1/email/**").permitAll()
				.requestMatchers("/api/v1/members/register").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/inquiries").permitAll() // 문의 접수 API만 인증 없이 접근 가능
				.requestMatchers( // logout, leave 제외
					"/api/v1/auth/login",
					"/api/v1/auth/reissue",
					"/api/v1/auth/oauth2/**"
				).permitAll()
				.requestMatchers( // WS 제외
					"/ws/**",
					"/sub/**",
					"/pub/**"
				).permitAll()
				.requestMatchers("/admin-login").permitAll() // 관리자 로그인 페이지는 인증 없이 접근 가능
				.requestMatchers("/admin-redirect").permitAll() // 관리자 리다이렉트 페이지는 인증 없이 접근 가능
				.requestMatchers("/admin/**").authenticated() // 관리자 페이지는 인증 필요 (AdminController에서 추가로 관리자 권한 검증)
				.anyRequest().authenticated() // 그 외 요청은 인증 필요
			)
			.formLogin(AbstractHttpConfigurer::disable) // 폼 로그인 비활성화
			.httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(adminAuthenticationEntryPoint) // 인증 실패 시 처리
			)
			.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 등록

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("http://localhost:3333");
		config.addAllowedOriginPattern("https://localhost:3333");
		config.addAllowedOriginPattern("https://comket.co.kr");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}
