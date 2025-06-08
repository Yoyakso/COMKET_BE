package com.yoyakso.comket.thread.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.thread.dto.ThreadMessageEditDto;
import com.yoyakso.comket.thread.service.ThreadMessageService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class ThreadMessageController {
	private final ThreadMessageService threadMessageService;
	private final MemberService memberService;

	@Operation(method = "PATCH", description = "스레드 메시지 수정 API")
	@PostMapping("/{ticketId}/edit")
	public ResponseEntity<Void> editThreadMessage(
		@PathVariable("ticketId") String ticketId,
		@RequestBody ThreadMessageEditDto requestData
	) {
		Member member = memberService.getAuthenticatedMember();
		if (!member.equals(requestData.getSenderMemberId())) {
			throw new CustomException("MEMBER_NOT_SENDER", "스레드 작성자가 아닙니다.");
		}

		threadMessageService.editMessage(requestData);
		return ResponseEntity.ok().build();
	}

}
