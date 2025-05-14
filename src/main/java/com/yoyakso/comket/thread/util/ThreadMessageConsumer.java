package com.yoyakso.comket.thread.util;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.service.ThreadMessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThreadMessageConsumer {

	private final ObjectMapper objectMapper;
	private final ThreadSocketHandler threadSocketHandler;
	private final ThreadMessageService threadMessageService;

	@KafkaListener(topics = "thread-ticket-*", groupId = "thread-consumer-group")
	public void consume(String messageJson) throws Exception {
		ThreadMessageDto messageDto = objectMapper.readValue(messageJson, ThreadMessageDto.class);
		Long ticketId = messageDto.getTicketId();

		// 1. WebSocket 브로드캐스트
		threadSocketHandler.sendToTicket(ticketId, messageDto);

		// 2. 비동기 DB 저장
		threadMessageService.saveAsync(messageDto);
	}
}
