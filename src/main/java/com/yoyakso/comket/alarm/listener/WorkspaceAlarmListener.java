package com.yoyakso.comket.alarm.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.workspace.event.WorkspaceInviteEvent;
import com.yoyakso.comket.workspace.event.WorkspaceRoleChangedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 워크스페이스 관련 이벤트를 수신하여 알람을 생성하는 리스너
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkspaceAlarmListener {

	private final AlarmService alarmService;

	/**
	 * 워크스페이스 초대 이벤트 처리
	 */
	@EventListener
	public void handleWorkspaceInviteEvent(WorkspaceInviteEvent event) {
		log.info("워크스페이스 초대 이벤트 수신: workspaceId={}, memberId={}",
			event.getWorkspace().getId(), event.getInvitedMember().getId());

		// 워크스페이스 초대 알람 추가
		alarmService.addWorkspaceInviteAlarm(event.getInvitedMember(), event.getWorkspace());
	}

	/**
	 * 워크스페이스 역할 변경 이벤트 처리
	 */
	@EventListener
	public void handleWorkspaceRoleChangedEvent(WorkspaceRoleChangedEvent event) {
		log.info("워크스페이스 역할 변경 이벤트 수신: workspaceId={}, memberId={}, oldRole={}, newRole={}",
			event.getWorkspace().getId(), event.getMember().getId(), event.getOldRole(), event.getNewRole());

		// 워크스페이스 역할 변경 알람 추가
		alarmService.addWorkspaceRoleChangedAlarm(event.getMember(), event.getWorkspace(),
			event.getOldRole(), event.getNewRole());
	}
}