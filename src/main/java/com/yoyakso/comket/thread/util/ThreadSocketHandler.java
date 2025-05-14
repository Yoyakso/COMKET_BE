package com.yoyakso.comket.thread.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.service.ThreadMessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThreadSocketHandler extends TextWebSocketHandler {

	private final ThreadMessageProducer threadMessageProducer;
	private final ThreadMessageService threadMessageService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Map<Long, List<WebSocketSession>> sessionPool = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Map<String, Object> attrs = session.getAttributes();
		Long ticketId = (Long)attrs.get("ticketId");

		sessionPool.computeIfAbsent(ticketId, k -> new CopyOnWriteArrayList<>()).add(session);

		List<ThreadMessageDto> previousMessages = threadMessageService.getMessagesByTicketId(ticketId);
		String jsonPayload = objectMapper.writeValueAsString(previousMessages);
		session.sendMessage(new TextMessage(jsonPayload));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Map<String, Object> attrs = session.getAttributes();
		Long ticketId = (Long)attrs.get("ticketId");

		// 클라이언트로부터 받은 메시지 JSON → DTO
		ThreadMessageDto messageDto = objectMapper.readValue(message.getPayload(), ThreadMessageDto.class);

		// Kafka topic: thread-ticket-{ticketId}
		String topic = "thread-ticket-" + ticketId;

		// Kafka로 전송
		String serialized = objectMapper.writeValueAsString(messageDto);
		threadMessageProducer.sendMessage(topic, serialized);
	}

	public void sendToTicket(Long ticketId, ThreadMessageDto messageDto) throws IOException {
		List<WebSocketSession> sessions = sessionPool.get(ticketId);
		if (sessions == null)
			return;

		String payload = objectMapper.writeValueAsString(List.of(messageDto));
		TextMessage textMessage = new TextMessage(payload);

		for (WebSocketSession session : sessions) {
			if (session.isOpen()) {
				session.sendMessage(textMessage);
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Map<String, Object> attrs = session.getAttributes();
		Long ticketId = (Long)attrs.get("ticketId");

		List<WebSocketSession> sessions = sessionPool.get(ticketId);
		if (sessions != null) {
			sessions.remove(session);
		}
	}
}
