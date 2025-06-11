package com.yoyakso.comket.alarm.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.alarm.service.AlarmService;
import com.yoyakso.comket.thread.event.ThreadMentionedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreadAlarmListener {

	private final AlarmService alarmService;

	/**
	 * 스레드 멘션 이벤트 처리
	 */
	@EventListener
	public void handleThreadMentionedEvent(ThreadMentionedEvent event) {
		log.info("스레드 멘션 이벤트 수신: 스레드 Id={}, 멘션된 프로젝트 멤버 Id={}",
			event.getThreadMessage().getId(), event.getProjectMember().getId());

		// 스레드 멘션 알람 추가
		event.getThreadMessage().getMentions()
			.forEach(mentions -> alarmService.addThreadMentionAlarm(
				mentions.getThreadMessage(),
				mentions.getMentionedMember(),
				"스레드에 멘션되었습니다."
			));
	}
}