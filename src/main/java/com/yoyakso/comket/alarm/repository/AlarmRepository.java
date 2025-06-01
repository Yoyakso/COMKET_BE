package com.yoyakso.comket.alarm.repository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.alarm.entity.ProjectAlarm;
import com.yoyakso.comket.alarm.entity.TicketAlarm;
import com.yoyakso.comket.alarm.enums.TicketAlarmType;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.ticket.entity.Ticket;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AlarmRepository {
	private final RedisTemplate<String, String> redisTemplate;

	public List<ProjectAlarm> findProjectAlarmListByMemberAndProjectIdIn(Member member, List<Project> projectList) {
		//redis template를 사용하여 알람 정보를 조회하는 로직을 구현
		// Key 생성
		List<String> keys = projectList.stream()
			.map(project -> generateProjectKey(project.getId(), member.getId()))
			.toList();

		// Redis에서 알람 정보 조회
		List<String> alarmCounts = redisTemplate.opsForValue().multiGet(keys);

		// 결과를 ProjectAlarm 객체로 변환
		return projectList.stream()
			.map(project -> {
				String countStr = alarmCounts.get(keys.indexOf(generateProjectKey(project.getId(), member.getId())));
				Long count = countStr != null ? Long.parseLong(countStr) : 0L;
				return ProjectAlarm.builder()
					.member(member)
					.project(project)
					.count(count)
					.build();
			})
			.toList();
	}

	public List<TicketAlarm> findTicketAlarmListByMemberAndTicketIdIn(Member member, List<Ticket> ticketList) {
		//redis template를 사용하여 알람 정보를 조회하는 로직을 구현
		// Key 생성
		List<String> keys = ticketList.stream()
			.flatMap(ticket ->
				Arrays.stream(TicketAlarmType.values())
					.map(alarmType -> generateTicketKey(ticket.getId(), member.getId(), alarmType))
			)
			.toList();
		
		// Redis에서 알람 정보 조회
		return keys.stream()
			.filter(key -> redisTemplate.opsForValue().get(key) != null) // Redis에서 값이 존재하는 경우만 필터링
			.map(key -> {
				Ticket matchingTicket = ticketList.stream()
					.filter(ticket -> {
						String ticketIdFromKey = key.split(":")[1]; // key에서 ticketId 추출
						return ticket.getId().toString().equals(ticketIdFromKey);
					})
					.findFirst()
					.orElse(null);

				if (matchingTicket == null) {
					return null;
				}
				TicketAlarmType alarmType = TicketAlarmType.valueOf(key.split(":")[5]); // key에서 alarmType 추출
				return TicketAlarm.builder()
					.member(member)
					.ticket(matchingTicket)
					.alarmType(alarmType)
					.build();
			})
			.filter(Objects::nonNull)
			.toList();
	}

	// 티켓 알람 읽음 처리
	public void markTicketAlarmAsRead(Member member, Long ticketId) {
		// 관련 알람 삭제 처리
		TicketAlarmType[] alarmTypes = TicketAlarmType.values();
		for (TicketAlarmType alarmType : alarmTypes) {
			String key = generateTicketKey(ticketId, member.getId(), alarmType);
			redisTemplate.delete(key);
		}
	}

	// Redis 프로젝트 알람 카운트 증가 or 생성
	public void incrementProjectAlarmCount(Member member, Long projectId) {
		String key = generateProjectKey(projectId, member.getId());
		redisTemplate.opsForValue().increment(key, 1);
	}

	// Redis 티켓 알람 생성
	public void createTicketAlarm(Member member, TicketAlarm ticketAlarm) {
		String key = generateTicketKey(ticketAlarm.getTicket().getId(), member.getId(), ticketAlarm.getAlarmType());
		redisTemplate.opsForValue().set(key, ticketAlarm.getAlarmMessage());
	}

	// Redis 키 생성 (프로젝트)
	private String generateProjectKey(Long projectId, Long memberId) {
		return "project:" + projectId + ":member:" + memberId + ":count";
	}

	// Redis 키 생성 (티켓)
	private String generateTicketKey(Long ticketId, Long memberId, TicketAlarmType alarmType) {
		return "ticket:" + ticketId + ":member:" + memberId + ":alarmType:" + alarmType.name();
	}

	public boolean existsTicketAlarm(TicketAlarm ticketAlarm) {
		String key = generateTicketKey(ticketAlarm.getTicket().getId(), ticketAlarm.getMember().getId(),
			ticketAlarm.getAlarmType());
		return redisTemplate.hasKey(key);
	}

	public void decrementProjectAlarmCount(Member member, Long projectId) {
		String key = generateProjectKey(projectId, member.getId());
		// 현재 카운트 조회
		String currentCountStr = redisTemplate.opsForValue().get(key);
		long currentCount = currentCountStr != null ? Long.parseLong(currentCountStr) : 0L;
		if (currentCount > 0) {
			redisTemplate.opsForValue().set(key, String.valueOf(currentCount - 1));
		} else {
			// 카운트가 0인 경우, 키를 삭제
			redisTemplate.delete(key);
		}
	}
}
