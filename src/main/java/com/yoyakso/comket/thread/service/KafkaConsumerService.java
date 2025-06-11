package com.yoyakso.comket.thread.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.util.ResourceJsonUtil;
import com.yoyakso.comket.thread.util.ThreadSocketHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService { // Kafka에서는

	private final ThreadMessageService threadMessageService;
	private final ObjectMapper objectMapper;
	private final ResourceJsonUtil resourceJsonUtil;
	@Lazy
	@Autowired
	private ThreadSocketHandler threadSocketHandler; // 순환참조 발생으로 의존성 지연 주입

	public void handleMessage(String jsonMessage, Long ticketId) {
		try {
			JsonNode rootNode = objectMapper.readTree(jsonMessage);
			String type = rootNode.get("type").asText();
			JsonNode dataNode = rootNode.get("data");

			if ("message_created".equals(type)) {
				ThreadMessageDto messageDto = objectMapper.treeToValue(dataNode, ThreadMessageDto.class);
				ThreadMessage savedEntity = threadMessageService.saveAsync(messageDto);
				List<String> resources = resourceJsonUtil.fromJson(savedEntity.getResources());
				ThreadMessageDto updatedDto = ThreadMessageDto.builder()
					.threadId(savedEntity.getId())   // 저장된 id로 세팅
					.ticketId(savedEntity.getTicketId())
					.senderWorkspaceMemberId(savedEntity.getSenderWorkspaceMemberId())
					.senderName(messageDto.getSenderName()) // 이름은 요청에서
					.content(savedEntity.getContent())
					.resources(resources)
					.sentAt(savedEntity.getSentAt())
					.isModified(savedEntity.getIsModified())
					.parentThreadId(savedEntity.getParentThreadId())
					.mentionedProjectMemberIds(extractMentionIds(savedEntity))
					.build();
				threadSocketHandler.sendToTicket(ticketId, updatedDto); // WebSocket broadcast
			} else if ("message_updated".equals(type)) {
				ThreadMessageDto updatedMessage = objectMapper.treeToValue(dataNode, ThreadMessageDto.class);
				threadSocketHandler.sendToTicket(ticketId, updatedMessage); // DB 저장 없이 broadcast만
			} else if ("message_deleted".equals(type)) { // Delete 등의 상태도 추가
				ThreadMessageDto deletedMessage = objectMapper.treeToValue(dataNode, ThreadMessageDto.class);
				threadSocketHandler.sendToTicket(ticketId, deletedMessage);
			} else if ("message_replied".equals(type)) {
				ThreadMessageDto replyMessage = objectMapper.treeToValue(dataNode, ThreadMessageDto.class);
				threadSocketHandler.sendToTicket(ticketId, replyMessage);
			} else {
				System.out.println("@@@ 알 수 없는 메시지 타입 수신: " + type);
			}
		} catch (Exception e) {
			// TODO: 로그 추가
			System.out.println("@@@ Kafka 메시지 처리 실패" + e.getMessage());
		}
	}

	private List<Long> extractMentionIds(ThreadMessage message) {
		if (message.getMentions() == null)
			return Collections.emptyList();
		return message.getMentions().stream()
			.map(mention -> mention.getMentionedMember().getId()) // ProjectMember ID 추출
			.toList();
	}
}
