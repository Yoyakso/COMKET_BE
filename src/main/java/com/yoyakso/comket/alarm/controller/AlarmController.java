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
import com.yoyakso.comket.alarm.dto.response.ProjectAlarmResponse;
import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.ProjectEventAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.entity.WorkspaceAlarm;
import com.yoyakso.comket.alarm.enums.ProjectAlarmType;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.enums.WorkspaceAlarmType;
import com.yoyakso.comket.alarm.mapper.AlarmMapper;
import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.service.ThreadMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/alarm")
public class AlarmController {

	private final AlarmService alarmService;
	private final MemberService memberService;
	private final AlarmMapper alarmMapper;
	private final ThreadMessageService threadMessageService;
	private final ProjectMemberService projectMemberService;

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

	// 프로젝트 이벤트 알람 조회 API
	@GetMapping("/project-event")
	public ResponseEntity<List<ProjectAlarmResponse>> getProjectEventAlarms(
		@RequestParam Long projectId
	) {
		Member member = memberService.getAuthenticatedMember();
		List<ProjectEventAlarm> projectEventAlarmList = alarmService.getProjectEventAlarms(member, projectId);
		return ResponseEntity.ok(
			projectEventAlarmList.stream()
				.map(alarmMapper::toProjectAlarmResponse)
				.toList()
		);
	}

	// 프로젝트 이벤트 알람 읽음 처리 API
	@PutMapping("/project-event/read")
	public ResponseEntity<Void> markProjectEventAlarmAsRead(
		@RequestParam Long projectId,
		@RequestParam ProjectAlarmType alarmType
	) {
		Member member = memberService.getAuthenticatedMember();
		alarmService.markProjectEventAlarmAsRead(member, projectId, alarmType);
		return ResponseEntity.noContent().build();
	}

	// 스레드 멘션 알람 읽음 처리
	@PutMapping("/thread-mentioned/read")
	public ResponseEntity<Void> markThreadMentionedAlarmAsRead(
		@RequestParam Long threadMessageId,
		@RequestParam Long projectMemberId
	) {
		ThreadMessage message = threadMessageService.getThreadMessageById(threadMessageId);
		ProjectMember mentionedProjectMember = projectMemberService.getProjectMemberByProjectMemberId(projectMemberId);

		alarmService.markThreadAlarmAsRead(message, mentionedProjectMember);
		return ResponseEntity.noContent().build();
	}
}
