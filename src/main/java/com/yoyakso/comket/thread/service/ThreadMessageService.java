package com.yoyakso.comket.thread.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.thread.dto.ThreadMessageDto;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThreadMessageService {

	private final ThreadMessageRepository threadMessageRepository;
	private final MemberService memberService;

	public List<ThreadMessageDto> getMessagesByTicketId(Long ticketId) {
		List<ThreadMessage> messages = threadMessageRepository.findAllByTicketIdOrderBySentAtAsc(ticketId);

		return messages.stream().map(message -> {
			String senderName = memberService.findMemberNameById(message.getSenderMemberId());

			return ThreadMessageDto.builder()
				.ticketId(message.getTicketId())
				.senderMemberId(message.getSenderMemberId())
				.senderName(senderName)
				.content(message.getContent())
				.sentAt(message.getSentAt())
				.build();
		}).toList();
	}

	@Async("threadDbExecutor")
	public void saveAsync(ThreadMessageDto dto) {
		ThreadMessage entity = ThreadMessage.builder()
			.ticketId(dto.getTicketId())
			.senderMemberId(dto.getSenderMemberId())
			.content(dto.getContent())
			.sentAt(dto.getSentAt()) // 클라이언트 or Kafka timestamp 기준
			.build();

		threadMessageRepository.save(entity);
	}
}
