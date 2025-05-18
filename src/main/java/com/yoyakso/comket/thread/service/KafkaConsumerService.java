package com.yoyakso.comket.thread.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.util.ThreadSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService { // Kafka에서는

	private final ThreadMessageService threadMessageService;
	private final ObjectMapper objectMapper;
	@Lazy
	@Autowired
	private ThreadSocketHandler threadSocketHandler; // 순환참조 발생으로 의존성 지연 주입

	public void handleMessage(String jsonMessage, Long ticketId) {
		try {
			ThreadMessageDto messageDto = objectMapper.readValue(jsonMessage, ThreadMessageDto.class);
			threadMessageService.saveAsync(messageDto);
			threadSocketHandler.sendToTicket(ticketId, messageDto);
		} catch (Exception e) {
			// TODO: 로그 추가
			System.out.println("@@@ Kafka 메시지 처리 실패" + e.getMessage());
		}
	}
}
