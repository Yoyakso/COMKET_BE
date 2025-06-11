package com.yoyakso.comket.thread.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.thread.dto.ThreadMessageDeleteRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.dto.ThreadMessageEditRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageReplyRequestDto;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.entity.ThreadMessageMention;
import com.yoyakso.comket.thread.enums.ThreadMessageState;
import com.yoyakso.comket.thread.event.ThreadMentionedEvent;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;
import com.yoyakso.comket.thread.util.ResourceJsonUtil;
import com.yoyakso.comket.thread.util.ThreadMessageProducer;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThreadMessageService {

	private final ThreadMessageRepository threadMessageRepository;
	private final ThreadMessageProducer threadMessageProducer;
	private final ObjectMapper objectMapper;
	private final ResourceJsonUtil resourceJsonUtil;
	private final WorkspaceMemberService workspaceMemberService;
	private final ProjectMemberRepository projectMemberRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public List<ThreadMessageDto> getMessagesByTicketId(Long ticketId) {
		List<ThreadMessage> messages = threadMessageRepository.findAllByTicketIdOrderBySentAtAsc(ticketId);

		return messages.stream().map(message -> {
			String senderName = workspaceMemberService.getWorkspaceMemberById(message.getSenderWorkspaceMemberId())
				.getNickName();
			List<String> resources = resourceJsonUtil.fromJson(message.getResources());

			return ThreadMessageDto.builder()
				.ticketId(message.getTicketId())
				.parentThreadId(message.getParentThreadId())
				.threadId(message.getId())
				.senderWorkspaceMemberId(message.getSenderWorkspaceMemberId())
				.senderName(senderName)
				.content(message.getContent())
				.resources(resources)
				.sentAt(message.getSentAt())
				.isModified(message.getIsModified())
				.mentionedProjectMemberIds(extractMentionIds(message))
				.build();
		}).toList();
	}

	public ThreadMessage getThreadMessageById(Long threadMessageId) {
		return threadMessageRepository.findById(threadMessageId).orElse(null);
	}

	// threadId를 넘겨줘야 하기 때문에 async -> sync 변경 / 추후 개선
	// @Async("threadDbExecutor")
	public ThreadMessage saveAsync(ThreadMessageDto dto) {
		String resourcesJson = resourceJsonUtil.toJson(dto.getResources());

		ThreadMessage entity = ThreadMessage.builder()
			.ticketId(dto.getTicketId())
			.parentThreadId(dto.getParentThreadId())
			.senderWorkspaceMemberId(dto.getSenderWorkspaceMemberId())
			.content(dto.getContent())
			.isModified(false)
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.resources(resourcesJson)
			.build();

		ThreadMessage saved = threadMessageRepository.save(entity);
		// Mentions 저장
		if (dto.getMentionedProjectMemberIds() != null && !dto.getMentionedProjectMemberIds().isEmpty()) {
			List<ThreadMessageMention> mentions = dto.getMentionedProjectMemberIds().stream()
				.map(id -> {
					ProjectMember pm = projectMemberRepository.findById(id)
						.orElseThrow(() -> new CustomException("PROJECT_MEMBER_NOT_FOUND", "존재하지 않는 멤버입니다."));
					eventPublisher.publishEvent(new ThreadMentionedEvent(saved, pm));
					return ThreadMessageMention.builder()
						.threadMessage(saved)
						.mentionedMember(pm)
						.build();
				}).toList();

			saved.setMentions(mentions);
			threadMessageRepository.save(saved);
		}

		return saved;
	}

	@Transactional
	public void editMessage(ThreadMessageEditRequestDto dto) {
		ThreadMessage message = threadMessageRepository.findById(dto.getThreadId())
			.orElseThrow(() -> new CustomException("THREAD_NOT_FOUND", "스레드를 찾을 수 없습니다."));

		message.getMentions().clear();

		if (dto.getMentionedProjectMemberIds() != null && !dto.getMentionedProjectMemberIds().isEmpty()) {
			List<ThreadMessageMention> newMentions = dto.getMentionedProjectMemberIds().stream()
				.map(id -> {
					ProjectMember member = projectMemberRepository.findById(id)
						.orElseThrow(() -> new CustomException("PROJECT_MEMBER_NOT_FOUND", "존재하지 않는 멤버입니다."));
					return ThreadMessageMention.builder()
						.threadMessage(message)
						.mentionedMember(member)
						.build();
				}).toList();
			message.getMentions().clear();
			message.getMentions().addAll(newMentions);
		}

		String senderName = workspaceMemberService.getWorkspaceMemberById(message.getSenderWorkspaceMemberId())
			.getNickName();
		String resourcesJson = resourceJsonUtil.toJson(dto.getResources());

		message.editContent(dto.getContent());
		message.setIsModified();
		message.editResources(resourcesJson);

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.ticketId(message.getTicketId())
			.threadId(message.getId())
			.parentThreadId(null)
			.senderWorkspaceMemberId(message.getSenderWorkspaceMemberId())
			.senderName(senderName)
			.content(message.getContent())
			.resources(dto.getResources())
			.sentAt(message.getSentAt())
			.isModified(true)
			.messageState(ThreadMessageState.UPDATE)
			.mentionedProjectMemberIds(extractMentionIds(message))
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
			.senderWorkspaceMemberId(dto.getSenderWorkspaceMemberId())
			.content(dto.getReply())
			.resources(resourcesJson)
			.isModified(false)
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.build();

		ThreadMessage savedReply = threadMessageRepository.save(entity);

		if (dto.getMentionedProjectMemberIds() != null && !dto.getMentionedProjectMemberIds().isEmpty()) {
			List<ThreadMessageMention> mentions = dto.getMentionedProjectMemberIds().stream()
				.map(id -> {
					ProjectMember member = projectMemberRepository.findById(id)
						.orElseThrow(() -> new CustomException("PROJECT_MEMBER_NOT_FOUND", "존재하지 않는 멤버입니다."));
					return ThreadMessageMention.builder()
						.threadMessage(savedReply)
						.mentionedMember(member)
						.build();
				}).collect(Collectors.toCollection(ArrayList::new));
			savedReply.setMentions(mentions);
			threadMessageRepository.save(savedReply);
		}

		String senderName = workspaceMemberService.getWorkspaceMemberById(message.getSenderWorkspaceMemberId())
			.getNickName();

		ThreadMessageDto responseMessage = ThreadMessageDto.builder()
			.ticketId(savedReply.getTicketId())
			.parentThreadId(savedReply.getParentThreadId())
			.senderWorkspaceMemberId(savedReply.getSenderWorkspaceMemberId())
			.senderName(senderName)
			.content(savedReply.getContent())
			.resources(dto.getResources())
			.sentAt(savedReply.getSentAt())
			.isModified(false)
			.mentionedProjectMemberIds(extractMentionIds(savedReply))
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

	private List<Long> extractMentionIds(ThreadMessage message) {
		if (message.getMentions() == null)
			return Collections.emptyList();
		return message.getMentions().stream()
			.map(mention -> mention.getMentionedMember().getId()) // ProjectMember ID 추출
			.toList();
	}

}
