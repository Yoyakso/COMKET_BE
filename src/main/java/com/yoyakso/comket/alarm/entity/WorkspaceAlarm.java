package com.yoyakso.comket.alarm.entity;

import com.yoyakso.comket.alarm.enums.WorkspaceAlarmType;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceAlarm {
	// 워크스페이스 ID
	private Workspace workspace;

	// 멤버 ID
	private Member member;

	// 워크스페이스 관련 알림 종류
	private WorkspaceAlarmType alarmType;

	// 알람 메세지
	private String alarmMessage = "";
}