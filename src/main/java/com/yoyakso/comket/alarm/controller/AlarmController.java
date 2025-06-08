package com.yoyakso.comket.alarm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.alarm.dto.response.AlarmTicketResponse;
import com.yoyakso.comket.alarm.dto.response.AlarmWorkspaceCountResponse;
import com.yoyakso.comket.alarm.dto.response.AlarmWorkspaceResponse;
import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.entity.WorkspaceAlarm;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.enums.WorkspaceAlarmType;
import com.yoyakso.comket.alarm.mapper.AlarmMapper;
import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarm")
public class AlarmController {

	private final AlarmService alarmService;
	private final MemberService memberService;
	private final AlarmMapper alarmMapper;

	// 워크스페이스 내부 프로젝트별 알람 count 조회 API
	@GetMapping("/project/count")
	public ResponseEntity<AlarmWorkspaceCountResponse> getAlarmCountByWorkspace(
		@RequestParam Long workspaceId
	) {
		Member member = memberService.getAuthenticatedMember();
		List<ProjectAlarm> projectAlarmList = alarmService.getProjectAlarmsByWorkspace(member, workspaceId);
		return ResponseEntity.ok(alarmMapper.toAlarmWorkspaceResponse(member, workspaceId, projectAlarmList));
	}

	// 프로젝트 내부 티켓 알람 조회 API
	@GetMapping("/tickets")
	public ResponseEntity<List<AlarmTicketResponse>> getAlarmTicketsByProject(
		@RequestParam Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();

		List<TicketAlarm> ticketAlarmList = alarmService.getTicketAlarmsByProject(member, projectId);
		return ResponseEntity.ok(
			ticketAlarmList.stream()
				.map(alarmMapper::toAlarmTicketResponse)
				.toList()
		);
	}

	// 티켓 알람 읽음 처리 API
	@PutMapping("/ticket/read")
	public ResponseEntity<Void> markTicketAlarmAsRead(
		@RequestParam Long ticketId
	) {
		Member member = memberService.getAuthenticatedMember();
		alarmService.markTicketAlarmAsRead(member, ticketId);
		return ResponseEntity.noContent().build();
	}

	// 테스트용 티켓 알람 추가 API
	@PostMapping("/tickets")
	public ResponseEntity<Void> addTicketAlarm(
		@RequestParam Long ticketId,
		@RequestParam TicketAlarmType alarmType,
		@RequestParam(required = false) String alarmMessage
	) {
		Member member = memberService.getAuthenticatedMember();
		alarmService.addTicketAlarm(member, ticketId, alarmType, alarmMessage);
		return ResponseEntity.ok().build();
	}

	// 워크스페이스 알람 조회 API
	@GetMapping("/workspace")
	public ResponseEntity<List<AlarmWorkspaceResponse>> getWorkspaceAlarms(
		@RequestParam Long workspaceId
	) {
		Member member = memberService.getAuthenticatedMember();
		List<WorkspaceAlarm> workspaceAlarmList = alarmService.getWorkspaceAlarms(member, workspaceId);
		return ResponseEntity.ok(
			workspaceAlarmList.stream()
				.map(alarmMapper::toAlarmWorkspaceResponse)
				.toList()
		);
	}

	// 워크스페이스 알람 읽음 처리 API
	@PutMapping("/workspace/read")
	public ResponseEntity<Void> markWorkspaceAlarmAsRead(
		@RequestParam Long workspaceId,
		@RequestParam WorkspaceAlarmType alarmType
	) {
		Member member = memberService.getAuthenticatedMember();
		alarmService.markWorkspaceAlarmAsRead(member, workspaceId, alarmType);
		return ResponseEntity.noContent().build();
	}

}
