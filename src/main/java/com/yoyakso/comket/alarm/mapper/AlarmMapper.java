package com.yoyakso.comket.alarm.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yoyakso.comket.alarm.dto.response.AlarmProjectCountResponse;
import com.yoyakso.comket.alarm.dto.response.AlarmTicketResponse;
import com.yoyakso.comket.alarm.dto.response.AlarmWorkspaceCountResponse;
import com.yoyakso.comket.alarm.dto.response.AlarmWorkspaceResponse;
import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.entity.WorkspaceAlarm;
import com.yoyakso.comket.member.entity.Member;

@Component
public class AlarmMapper {

	public AlarmWorkspaceCountResponse toAlarmWorkspaceResponse(Member member, Long workspaceId,
		List<ProjectAlarm> projectAlarmList) {
		return AlarmWorkspaceCountResponse.builder()
			.memberId(member.getId())
			.workspaceId(workspaceId)
			.projectAlarmList(projectAlarmList.stream()
				.map(this::toAlarmProjectResponse)
				.toList())
			.build();
	}

	public AlarmProjectCountResponse toAlarmProjectResponse(ProjectAlarm projectAlarm) {
		return AlarmProjectCountResponse.builder()
			.memberId(projectAlarm.getMember().getId())
			.projectId(projectAlarm.getProject().getId())
			.projectName(projectAlarm.getProject().getName())
			.alarmCount(projectAlarm.getCount())
			.build();
	}

	public AlarmTicketResponse toAlarmTicketResponse(TicketAlarm ticketAlarm) {
		return AlarmTicketResponse.builder()
			.memberId(ticketAlarm.getMember().getId())
			.ticketId(ticketAlarm.getTicket().getId())
			.ticketAlarmType(ticketAlarm.getAlarmType())
			.alarmMessage(ticketAlarm.getAlarmMessage())
			.build();
	}

	public AlarmWorkspaceResponse toAlarmWorkspaceResponse(WorkspaceAlarm workspaceAlarm) {
		return AlarmWorkspaceResponse.builder()
			.memberId(workspaceAlarm.getMember().getId())
			.workspaceId(workspaceAlarm.getWorkspace().getId())
			.workspaceName(workspaceAlarm.getWorkspace().getName())
			.alarmType(workspaceAlarm.getAlarmType())
			.alarmMessage(workspaceAlarm.getAlarmMessage())
			.build();
	}
}
