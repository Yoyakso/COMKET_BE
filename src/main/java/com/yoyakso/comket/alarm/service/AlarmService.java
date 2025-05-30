package com.yoyakso.comket.alarm.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.alarm.dto.external.AlarmProjectResponse;
import com.yoyakso.comket.alarm.dto.external.AlarmWorkspaceResponse;
import com.yoyakso.comket.alarm.entity.Alarm;
import com.yoyakso.comket.alarm.repository.AlarmRepository;
import com.yoyakso.comket.member.entity.Member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlarmService {
	private final AlarmRepository alarmRepository;

	// alarm count 추가 메서드
	// 해당되는 row가 없다면 추가
	@Transactional
	public void incrementAlarmCount(Long memberId, Long projectId) {
		// Alarm 엔티티를 memberId와 projectId로 조회
		Alarm alarm = alarmRepository.findByMemberIdAndProjectId(memberId, projectId)
			.orElseGet(() -> {
				// 해당 row가 없으면 새로 생성
				Alarm newAlarm = Alarm.builder()
					.memberId(memberId)
					.projectId(projectId)
					.count(0L)
					.build();
				return alarmRepository.save(newAlarm);
			});

		// count 증가
		alarm.setCount(alarm.getCount() + 1);
		alarmRepository.save(alarm);
	}

	// 알람 카운트 초기화 메서드
	@Transactional
	public void resetAlarmCount(Member member, Long projectId) {
		// Alarm 엔티티를 memberId와 projectId로 조회
		Alarm alarm = alarmRepository.findByMemberIdAndProjectId(member.getId(), projectId)
			.orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다."));

		// count 초기화
		alarm.setCount(0L);
		alarmRepository.save(alarm);
	}

	// 알람 카운트 조회 메서드
	@Transactional
	public AlarmProjectResponse getAlarmCountByProject(Member member, Long projectId) {
		// Alarm 엔티티를 memberId와 projectId로 조회
		Alarm alarm = alarmRepository.findByMemberIdAndProjectId(member.getId(), projectId)
			.orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다."));

		return AlarmProjectResponse.builder()
			.memberId(alarm.getMemberId())
			.projectId(alarm.getProjectId())
			.alarmCount(alarm.getCount())
			.build();
	}

	// 워크스페이스 내부 프로젝트별 알람 카운트 조회 메서드
	@Transactional
	public AlarmWorkspaceResponse getAlarmCountByWorkspace(Member member, Long workspaceId) {
		// 워크스페이스 내부의 모든 프로젝트에 대한 알람 카운트를 조회하고, 각 프로젝트 별로 AlarmProjectResponse 객체를 생성
		List<Alarm> alarmList = alarmRepository.findByMemberIdAndWorkspaceId(member.getId(), workspaceId);

		// AlarmWorkspaceResponse 객체 생성 및 반환
		return AlarmWorkspaceResponse.builder()
			.memberId(member.getId())
			.workspaceId(workspaceId)
			.projectAlarmList(alarmList.stream()
				.map(alarm -> AlarmProjectResponse.builder()
					.projectId(alarm.getProjectId())
					.alarmCount(alarm.getCount())
					.memberId(alarm.getId())
					.build())
				.toList())
			.build();
	}
}
