package com.yoyakso.comket.alarm.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.project.event.ProjectInviteEvent;

import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 관련 이벤트를 수신하여 알람을 생성하는 리스너
 */
@Component
@RequiredArgsConstructor
public class ProjectAlarmListener {

    private final AlarmService alarmService;

    /**
     * 프로젝트 초대 이벤트 처리
     */
    @EventListener
    public void handleProjectInviteEvent(ProjectInviteEvent event) {
        // 프로젝트 초대 알람 추가
        alarmService.addProjectInviteAlarm(event.getInvitedMember(), event.getProject());
    }
}