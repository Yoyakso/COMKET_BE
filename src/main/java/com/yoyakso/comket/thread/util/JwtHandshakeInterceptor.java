package com.yoyakso.comket.thread.util;

import java.net.URI;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.ticket.service.TicketService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

	private final JwtTokenProvider jwtTokenProvider;
	private final ProjectMemberService projectMemberService;
	private final TicketService ticketService;

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
		URI uri = request.getURI();
		MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

		String token = params.getFirst("token");
		Long ticketId = Long.valueOf(params.getFirst("ticketId"));

		if (token == null || ticketId == null) {
			throw new CustomException("INVALID_HANDSHAKE_REQUEST", "웹소켓 인증에 실패했습니다.");
		}

		try {
			String email = jwtTokenProvider.getEmailFromToken(token);
			System.out.println("[Interceptor] - email: " + email);
			Long projectId = ticketService.getProjectIdByTicketId(ticketId);
			System.out.println("[Interceptor] - projectId: " + projectId);
			Long projectMemberId = projectMemberService.findByMemberEmail(projectId, email).getId();
			System.out.println("[Interceptor] - projectMemberId: " + projectMemberId);

			attributes.put("memberId", projectMemberId);
			attributes.put("email", email);
			attributes.put("ticketId", ticketId);
			return true;
		} catch (Exception e) {
			throw new CustomException("HANDSHAKE_FAILED", "웹소켓 연결에 실패했습니다." + e.getMessage());
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception ex) {
		// 추후 사용
	}
}
