package com.yoyakso.comket.alarm.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.ProjectEventAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.entity.WorkspaceAlarm;
import com.yoyakso.comket.alarm.enums.ProjectAlarmType;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.alarm.enums.WorkspaceAlarmType;
import com.yoyakso.comket.alarm.repository.AlarmRepository;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.service.InternalTicketService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmService {
	private final WorkspaceService workspaceService;
	private final ProjectService projectService;
	private final InternalTicketService internalTicketService;
	private final AlarmRepository alarmRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final FCMService fcmService;

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
		List<Ticket> ticketList = internalTicketService.getTickets(project.getName(), member);

		return alarmRepository.findTicketAlarmListByMemberAndTicketIdIn(member, ticketList);
	}

	// 티켓 알람 읽음 처리
	public void markTicketAlarmAsRead(Member member, Long ticketId) {
		// 티켓 접근 가능 여부 확인
		Ticket ticket = internalTicketService.getTicketByIdAndMember(ticketId, member);
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

		// FCM 알림 전송
		sendFcmNotification(member, ticket, alarmType, alarmMessage);
	}

	// FCM 알림 전송 메서드
	private void sendFcmNotification(Member member, Ticket ticket, TicketAlarmType alarmType, String alarmMessage) {
		// 사용자의 FCM 토큰 조회
		String fcmToken = fcmService.getFcmToken(member.getId());

		// FCM 토큰이 있는 경우에만 알림 전송
		if (fcmToken != null && !fcmToken.isEmpty()) {
			try {
				// 알림 데이터 설정
				Map<String, String> data = new HashMap<>();
				data.put("ticketId", ticket.getId().toString());
				data.put("projectId", ticket.getProject().getId().toString());
				data.put("alarmType", alarmType.name());

				// FCM 알림 전송
				fcmService.sendNotification(
					fcmToken,
					"티켓 알림",
					alarmMessage,
					data
				);

				log.info("FCM 알림 전송 완료: memberId={}, ticketId={}, alarmType={}",
					member.getId(), ticket.getId(), alarmType);
			} catch (Exception e) {
				// 알림 전송 실패 시 로그만 남기고 예외는 전파하지 않음
				log.error("FCM 알림 전송 실패: memberId={}, ticketId={}, alarmType={}, error={}",
					member.getId(), ticket.getId(), alarmType, e.getMessage());
			}
		}
	}

	// 테스트용 티켓 알람 추가 API
	public void addTicketAlarm(Member member, Long ticketId, TicketAlarmType alarmType, String alarmMessage) {
		Ticket ticket = internalTicketService.getTicketByIdAndMember(ticketId, member);
		addTicketAlarm(member, ticket, alarmType, alarmMessage);
	}

	// 워크스페이스 알람 조회
	public List<WorkspaceAlarm> getWorkspaceAlarms(Member member, Long workspaceId) {
		// 워크스페이스 접근 가능 여부 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, member);

		return alarmRepository.findWorkspaceAlarmsByMember(member, workspace);
	}

	// 프로젝트 이벤트 알람 조회
	public List<ProjectEventAlarm> getProjectEventAlarms(Member member, Long projectId) {
		// 프로젝트 접근 가능 여부 확인
		Project project = projectService.getProjectByProjectId(projectId, member);

		return alarmRepository.findProjectEventAlarmsByMember(member, project);
	}

	// 워크스페이스 알람 읽음 처리
	public void markWorkspaceAlarmAsRead(Member member, Long workspaceId, WorkspaceAlarmType alarmType) {
		// 워크스페이스 접근 가능 여부 확인
		workspaceService.getWorkspaceById(workspaceId, member);

		alarmRepository.markWorkspaceAlarmAsRead(member, workspaceId, alarmType);
	}

	// 프로젝트 이벤트 알람 읽음 처리
	public void markProjectEventAlarmAsRead(Member member, Long projectId, ProjectAlarmType alarmType) {
		// 프로젝트 접근 가능 여부 확인
		projectService.getProjectByProjectId(projectId, member);

		alarmRepository.markProjectEventAlarmAsRead(member, projectId, alarmType);
	}

	// 워크스페이스 초대 알람 추가
	public void addWorkspaceInviteAlarm(Member member, Workspace workspace) {
		// 워크스페이스 알람 생성
		WorkspaceAlarm workspaceAlarm = WorkspaceAlarm.builder()
			.member(member)
			.workspace(workspace)
			.alarmType(WorkspaceAlarmType.WORKSPACE_INVITE)
			.alarmMessage(workspace.getName() + "에 초대되었습니다.")
			.build();

		// 워크스페이스 알람 저장
		alarmRepository.createWorkspaceAlarm(member, workspaceAlarm);

		// FCM 알림 전송
		sendWorkspaceFcmNotification(member, workspace, WorkspaceAlarmType.WORKSPACE_INVITE,
			workspaceAlarm.getAlarmMessage());
	}

	// 프로젝트 초대 알람 추가
	public void addProjectInviteAlarm(Member member, Project project) {
		// 프로젝트 알람 생성
		ProjectEventAlarm projectEventAlarm = ProjectEventAlarm.builder()
			.member(member)
			.project(project)
			.alarmType(ProjectAlarmType.PROJECT_INVITE)
			.alarmMessage(project.getName() + " 프로젝트에 멤버로 추가되었습니다.")
			.build();

		// 프로젝트 알람 저장
		alarmRepository.createProjectEventAlarm(member, projectEventAlarm);

		// FCM 알림 전송
		sendProjectEventFcmNotification(member, project, ProjectAlarmType.PROJECT_INVITE,
			projectEventAlarm.getAlarmMessage());
	}

	// 워크스페이스 역할 변경 알람 추가
	public void addWorkspaceRoleChangedAlarm(Member member, Workspace workspace, String oldRole, String newRole) {
		// 워크스페이스 알람 생성
		WorkspaceAlarm workspaceAlarm = WorkspaceAlarm.builder()
			.member(member)
			.workspace(workspace)
			.alarmType(WorkspaceAlarmType.WORKSPACE_POSITIONTYPE_CHANGED)
			.alarmMessage(workspace.getName() + "의 역할이 " + oldRole + "에서 " + newRole + "로 변경되었습니다.")
			.build();

		// 워크스페이스 알람 저장
		alarmRepository.createWorkspaceAlarm(member, workspaceAlarm);

		// FCM 알림 전송
		sendWorkspaceFcmNotification(member, workspace, WorkspaceAlarmType.WORKSPACE_POSITIONTYPE_CHANGED,
			workspaceAlarm.getAlarmMessage());
	}

	// FCM 워크스페이스 알림 전송 메서드
	private void sendWorkspaceFcmNotification(Member member, Workspace workspace, WorkspaceAlarmType alarmType,
		String alarmMessage) {
		// 사용자의 FCM 토큰 조회
		String fcmToken = fcmService.getFcmToken(member.getId());

		// FCM 토큰이 있는 경우에만 알림 전송
		if (fcmToken != null && !fcmToken.isEmpty()) {
			try {
				// 알림 데이터 설정
				Map<String, String> data = new HashMap<>();
				data.put("workspaceId", workspace.getId().toString());
				data.put("alarmType", alarmType.name());

				// FCM 알림 전송
				fcmService.sendNotification(
					fcmToken,
					"워크스페이스 알림",
					alarmMessage,
					data
				);
			} catch (Exception e) {
				// 알림 전송 실패 시 예외는 전파하지 않음
			}
		}
	}

	// FCM 프로젝트 이벤트 알림 전송 메서드
	private void sendProjectEventFcmNotification(Member member, Project project, ProjectAlarmType alarmType,
		String alarmMessage) {
		// 사용자의 FCM 토큰 조회
		String fcmToken = fcmService.getFcmToken(member.getId());

		// FCM 토큰이 있는 경우에만 알림 전송
		if (fcmToken != null && !fcmToken.isEmpty()) {
			try {
				// 알림 데이터 설정
				Map<String, String> data = new HashMap<>();
				data.put("projectId", project.getId().toString());
				data.put("alarmType", alarmType.name());

				// FCM 알림 전송
				fcmService.sendNotification(
					fcmToken,
					"프로젝트 알림",
					alarmMessage,
					data
				);
			} catch (Exception e) {
				// 알림 전송 실패 시 예외는 전파하지 않음
			}
		}
	}
}
