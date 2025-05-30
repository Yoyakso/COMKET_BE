package com.yoyakso.comket.alarm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.alarm.dto.external.AlarmProjectResponse;
import com.yoyakso.comket.alarm.dto.external.AlarmWorkspaceResponse;
import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.alarm.service.FcmService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarm")
public class AlarmController {

	private final FcmService fcmService;
	private final AlarmService alarmService;
	private final MemberService memberService;

	//fcm 알림 전송 API
	@PostMapping("/fcm/send")
	public String sendNotification(
		@RequestParam String title,
		@RequestParam String body,
		@RequestParam String token
	) {
		return fcmService.sendNotification(title, body, token);
	}

	// 워크스페이스별 alarm count 조회 API
	@GetMapping("/workspace/count")
	public AlarmWorkspaceResponse getAlarmCountByWorkspace(
		@RequestParam Long workspaceId
	) {
		Member member = memberService.getAuthenticatedMember();
		return alarmService.getAlarmCountByWorkspace(member, workspaceId);
	}

	// 프로젝트별 alarm count 조회 API
	@GetMapping("/project/count")
	public AlarmProjectResponse getAlarmCountByProject(
		@RequestParam Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		return alarmService.getAlarmCountByProject(member, projectId);
	}

	// 알림 초기화 API
	@PatchMapping("/project/reset")
	public void resetAlarmCountByProject(
		@RequestParam Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		alarmService.resetAlarmCount(member, projectId);
	}
}