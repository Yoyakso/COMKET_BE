package com.yoyakso.comket.alarm.entity;

import com.yoyakso.comket.alarm.enums.ProjectAlarmType;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectEventAlarm {
	// 프로젝트 ID
	private Project project;

	// 멤버 ID
	private Member member;

	// 프로젝트 관련 알림 종류
	private ProjectAlarmType alarmType;

	// 알람 메세지
	private String alarmMessage = "";
}