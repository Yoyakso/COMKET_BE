package com.yoyakso.comket.thread.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.thread.dto.ThreadMessageDeleteRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.dto.ThreadMessageEditRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageReplyRequestDto;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.enums.ThreadMessageState;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;
import com.yoyakso.comket.thread.util.ResourceJsonUtil;
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
	private final ResourceJsonUtil resourceJsonUtil;

	public List<ThreadMessageDto> getMessagesByTicketId(Long ticketId) {
		List<ThreadMessage> messages = threadMessageRepository.findAllByTicketIdOrderBySentAtAsc(ticketId);

		return messages.stream().map(message -> {
			String senderName = memberService.findMemberNameById(message.getSenderMemberId());
			List<String> resources = resourceJsonUtil.fromJson(message.getResources());

			return ThreadMessageDto.builder()
				.ticketId(message.getTicketId())
				.parentThreadId(message.getParentThreadId())
				.threadId(message.getId())
				.senderMemberId(message.getSenderMemberId())
				.senderName(senderName)
				.content(message.getContent())
				.resources(resources)
				.sentAt(message.getSentAt())
				.isModified(message.getIsModified())
				.build();
		}).toList();
	}

	// threadId를 넘겨줘야 하기 때문에 async -> sync 변경 / 추후 개선
	// @Async("threadDbExecutor")
	public ThreadMessage saveAsync(ThreadMessageDto dto) {
		String resourcesJson = resourceJsonUtil.toJson(dto.getResources());

		ThreadMessage entity = ThreadMessage.builder()
			.ticketId(dto.getTicketId())
			.parentThreadId(dto.getParentThreadId())
			.senderMemberId(dto.getSenderMemberId())
			.content(dto.getContent())
			.isModified(false)
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.resources(resourcesJson)
			.build();

		return threadMessageRepository.save(entity);
	}

	@Transactional
	public void editMessage(ThreadMessageEditRequestDto dto) {
		ThreadMessage message = threadMessageRepository.findById(dto.getThreadId())
			.orElseThrow(() -> new CustomException("THREAD_NOT_FOUND", "스레드를 찾을 수 없습니다."));

		String senderName = memberService.findMemberNameById(message.getSenderMemberId());
		String resourcesJson = resourceJsonUtil.toJson(dto.getResources());

		message.editContent(dto.getContent());
		message.setIsModified();
		message.editResources(resourcesJson);

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.ticketId(message.getTicketId())
			.threadId(message.getId())
			.parentThreadId(null)
			.senderMemberId(message.getSenderMemberId())
			.senderName(senderName)
			.content(message.getContent())
			.resources(dto.getResources())
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

	@Transactional
	public void deleteMessage(ThreadMessageDeleteRequestDto dto) {
		ThreadMessage message = threadMessageRepository.findById(dto.getThreadId())
			.orElseThrow(() -> new CustomException("THREAD_NOT_FOUND", "스레드를 찾을 수 없습니다."));

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.threadId(dto.getThreadId())
			.messageState(ThreadMessageState.DELETE)
			.build();

		threadMessageRepository.delete(message);

		String topic = "thread-ticket-" + message.getTicketId();
		try {
			Map<String, Object> messageWrapper = new HashMap<>();
			messageWrapper.put("type", "message_deleted"); // or "message_updated"
			messageWrapper.put("data", responseMessage); // 기존 DTO를 감싸줌

			String serializedEvent = objectMapper.writeValueAsString(messageWrapper);
			threadMessageProducer.sendMessage(topic, serializedEvent);
		} catch (Exception e) {
			throw new CustomException("THREAD_MESSAGE_DELETE_ERROR", "스레드 메시지 삭제에 실패했습니다.");
		}
	}

	@Transactional
	public void replyMessage(ThreadMessageReplyRequestDto dto) {
		ThreadMessage message = threadMessageRepository.findById(dto.getParentThreadId())
			.orElseThrow(() -> new CustomException("THREAD_NOT_FOUND", "스레드를 찾을 수 없습니다."));

		String resourcesJson = resourceJsonUtil.toJson(dto.getResources());

		ThreadMessage entity = ThreadMessage.builder()
			.ticketId(dto.getTicketId())
			.parentThreadId(dto.getParentThreadId())
			.senderMemberId(dto.getSenderMemberId())
			.content(dto.getReply())
			.resources(resourcesJson)
			.isModified(false)
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.build();

		ThreadMessage SavedThreadMeesage = threadMessageRepository.save(entity);

		String senderName = memberService.findMemberNameById(SavedThreadMeesage.getSenderMemberId());

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.ticketId(SavedThreadMeesage.getTicketId())
			.parentThreadId(SavedThreadMeesage.getParentThreadId())
			.senderMemberId(SavedThreadMeesage.getSenderMemberId())
			.senderName(senderName)
			.content(SavedThreadMeesage.getContent())
			.resources(dto.getResources())
			.sentAt(SavedThreadMeesage.getSentAt())
			.isModified(false)
			.build();

		String topic = "thread-ticket-" + message.getTicketId();
		try {
			Map<String, Object> messageWrapper = new HashMap<>();
			messageWrapper.put("type", "message_replied");
			messageWrapper.put("data", responseMessage);

			String serializedEvent = objectMapper.writeValueAsString(messageWrapper);
			threadMessageProducer.sendMessage(topic, serializedEvent);
		} catch (Exception e) {
			throw new CustomException("THREAD_MESSAGE_REPLY_ERROR", "스레드 메시지 답글 작성에 실패했습니다.");
		}
	}
}
