package com.yoyakso.comket.thread.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.thread.dto.ThreadMessageDeleteRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageEditRequestDto;
import com.yoyakso.comket.thread.dto.ThreadMessageReplyRequestDto;
import com.yoyakso.comket.thread.service.ThreadMessageService;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class ThreadMessageController {
	private final ThreadMessageService threadMessageService;
	private final MemberService memberService;
	private final WorkspaceMemberRepository workspaceMemberRepository;

	@Operation(method = "PATCH", description = "스레드 메시지 수정 API")
	@PatchMapping("/thread/edit")
	public ResponseEntity<Void> editThreadMessage(
		@RequestBody ThreadMessageEditRequestDto requestData
	) {
		Member member = memberService.getAuthenticatedMember();
		WorkspaceMember wm = workspaceMemberRepository.findByMemberIdAndWorkspaceId(
			member.getId(), requestData.getWorkspaceId()
		);
		if (!wm.getId().equals(requestData.getSenderWorkspaceMemberId())) {
			throw new CustomException("MEMBER_NOT_SENDER", "스레드 작성자가 아닙니다.");
		}

		threadMessageService.editMessage(requestData);
		return ResponseEntity.ok().build();
	}

	@Operation(method = "DELETE", description = "스레드 메시지 삭제 API")
	@DeleteMapping("/thread/delete")
	public ResponseEntity<Void> deleteThreadMessage(
		@RequestBody ThreadMessageDeleteRequestDto requestData
	) {
		Member member = memberService.getAuthenticatedMember();
		WorkspaceMember wm = workspaceMemberRepository.findByMemberIdAndWorkspaceId(
			member.getId(), requestData.getWorkspaceId()
		);
		if (!wm.getId().equals(requestData.getSenderWorkspaceMemberId())) {
			throw new CustomException("MEMBER_NOT_SENDER", "스레드 작성자가 아닙니다.");
		}

		threadMessageService.deleteMessage(requestData);
		return ResponseEntity.ok().build();
	}

	@Operation(method = "POST", description = "스레드 답글 작성 API")
	@PostMapping("/thread/reply")
	public ResponseEntity<Void> replyThreadMessage(
		@RequestBody ThreadMessageReplyRequestDto requestData
	) {
		Member member = memberService.getAuthenticatedMember();
		WorkspaceMember wm = workspaceMemberRepository.findByMemberIdAndWorkspaceId(
			member.getId(), requestData.getWorkspaceId()
		);
		if (!wm.getId().equals(requestData.getSenderWorkspaceMemberId())) {
			throw new CustomException("MEMBER_NOT_SENDER", "스레드 작성자가 아닙니다.");
		}

		threadMessageService.replyMessage(requestData);
		return ResponseEntity.ok().build();
	}
}
