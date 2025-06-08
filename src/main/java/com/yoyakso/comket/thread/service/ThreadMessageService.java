package com.yoyakso.comket.thread.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.dto.ThreadMessageEditRequestDto;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.enums.ThreadMessageState;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;
import com.yoyakso.comket.thread.util.ThreadMessageProducer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThreadMessageService {

	private final ThreadMessageRepository threadMessageRepository;
	private final MemberService memberService;
	private final ThreadMessageProducer threadMessageProducer;
	private final ObjectMapper objectMapper;

	public List<ThreadMessageDto> getMessagesByTicketId(Long ticketId) {
		List<ThreadMessage> messages = threadMessageRepository.findAllByTicketIdOrderBySentAtAsc(ticketId);

		return messages.stream().map(message -> {
			String senderName = memberService.findMemberNameById(message.getSenderMemberId());

			System.out.println("[test] - 3");
			return ThreadMessageDto.builder()
				.ticketId(message.getTicketId())
				.threadId(message.getId())
				.senderMemberId(message.getSenderMemberId())
				.senderName(senderName)
				.content(message.getContent())
				.sentAt(message.getSentAt())
				.isModified(message.getIsModified())
				.build();
		}).toList();
	}

	// threadId를 넘겨줘야 하기 때문에 async -> sync 변경 / 추후 개선
	// @Async("threadDbExecutor")
	public ThreadMessage saveAsync(ThreadMessageDto dto) {
		System.out.println("[test] - async");
		ThreadMessage entity = ThreadMessage.builder()
			.ticketId(dto.getTicketId())
			.senderMemberId(dto.getSenderMemberId())
			.content(dto.getContent())
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.isModified(false)
			.build();

		return threadMessageRepository.save(entity);
	}

	@Transactional
	public void editMessage(ThreadMessageEditRequestDto dto) {
		ThreadMessage message = threadMessageRepository.findById(dto.getThreadId())
			.orElseThrow(() -> new CustomException("THREAD_NOT_FOUND", "스레드를 찾을 수 없습니다."));

		String senderName = memberService.findMemberNameById(message.getSenderMemberId());

		message.editContent(dto.getContent());
		message.setIsModified();

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.ticketId(message.getTicketId())
			.threadId(message.getId())
			.senderMemberId(message.getSenderMemberId())
			.senderName(senderName)
			.content(message.getContent())
			.sentAt(message.getSentAt())
			.isModified(true)
			.messageState(ThreadMessageState.UPDATE)
			.build();

		String topic = "thread-ticket-" + message.getTicketId();
		try {
			Map<String, Object> messageWrapper = new HashMap<>();
			messageWrapper.put("type", "message_updated"); // or "message_updated"
			messageWrapper.put("data", responseMessage); // 기존 DTO를 감싸줌

			String serializedEvent = objectMapper.writeValueAsString(messageWrapper);
			threadMessageProducer.sendMessage(topic, serializedEvent);
		} catch (Exception e) {
			throw new CustomException("THREAD_MESSAGE_EDIT_ERROR", "스레드 메시지 수정에 실패했습니다.");
		}
	}
}
