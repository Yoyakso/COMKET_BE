package com.yoyakso.comket.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.yoyakso.comket.thread.util.JwtHandshakeInterceptor;
import com.yoyakso.comket.thread.util.ThreadSocketHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ThreadSocketHandler threadSocketHandler;
	private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(threadSocketHandler, "/ws/chat")
			.addInterceptors(jwtHandshakeInterceptor)
			.setAllowedOriginPatterns("*");
	}
}
