package com.yoyakso.comket.alarm.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.repository.AlarmRepository;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.service.TicketService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmService {
	private final WorkspaceService workspaceService;
	private final ProjectService projectService;
	private final TicketService ticketService;
	private final AlarmRepository alarmRepository;
	private final RedisTemplate<String, String> redisTemplate;

	// 워크스페이스별 알람 count 조회
	public List<ProjectAlarm> getProjectAlarmsByWorkspace(Member member, Long workspaceId) {
		// 워크스페이스 접근 가능 여부 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, member);

		// 프로젝트 ID List 조회
		List<Project> projectList = projectService.getProjectsByWorkspaceId(workspace.getId(), member);

		return alarmRepository.findProjectAlarmListByMemberAndProjectIdIn(member, projectList);
	}

	// 프로젝트별 티켓 알람 조회
	public List<TicketAlarm> getTicketAlarmsByProject(Member member, Long projectId) {
		// 프로젝트 접근 가능 여부 확인
		Project project = projectService.getProjectByProjectId(projectId, member);

		// 티켓 ID List 조회
		List<Ticket> ticketList = ticketService.getTickets(project.getName(), member);

		return alarmRepository.findTicketAlarmListByMemberAndTicketIdIn(member, ticketList);
	}

	// 티켓 알람 읽음 처리
	public void markTicketAlarmAsRead(Member member, Long ticketId) {
		// 티켓 접근 가능 여부 확인
		Ticket ticket = ticketService.getTicketByIdAndMember(ticketId, member);
		alarmRepository.markTicketAlarmAsRead(member, ticketId);
		// 티켓 알람 읽음 처리 후, 프로젝트 알람 카운트 감소
		alarmRepository.decrementProjectAlarmCount(member, ticket.getProject().getId());
	}

	// 티켓 알람 추가 로직
	public void addTicketAlarm(Member member, Ticket ticket, TicketAlarmType alarmType, String alarmMessage) {
		// 티켓 알람 생성
		TicketAlarm ticketAlarm = TicketAlarm.builder()
			.member(member)
			.ticket(ticket)
			.alarmType(alarmType)
			.alarmMessage(alarmMessage) // 알람 메세지는 필요에 따라 설정
			.build();

		boolean isAlarmExist = alarmRepository.existsTicketAlarm(ticketAlarm);
		// 티켓 알람 저장
		alarmRepository.createTicketAlarm(member, ticketAlarm);

		if (!isAlarmExist) {
			// 프로젝트에 대한 알람 카운트 증가
			alarmRepository.incrementProjectAlarmCount(member, ticket.getProject().getId());
		}
	}

	// 테스트용 티켓 알람 추가 API
	public void addTicketAlarm(Member member, Long ticketId, TicketAlarmType alarmType, String alarmMessage) {
		Ticket ticket = ticketService.getTicketByIdAndMember(ticketId, member);
		addTicketAlarm(member, ticket, alarmType, alarmMessage);
	}
}
