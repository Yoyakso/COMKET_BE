package com.yoyakso.comket.admin.dto.response;

import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationStatisticsResponse {

	// 전체 워크스페이스 수
	private long totalWorkspaces;

	// 전체 프로젝트 수
	private long totalProjects;

	// 전체 유저 수
	private long totalMembers;

	// 전체 티켓 수
	private long totalTickets;

	// 하루 평균 티켓 생성 수
	private double avgDailyTickets;

	// 하루 평균 유저 가입 수
	private double avgDailySignups;

	// 활성 이용자 수
	private long activeUsers;

	// 이탈율
	private double churnRate;

	// 전체 워크스페이스 사용 요금
	private long totalWorkspaceUsageFee;

	// 일별 티켓 생성 추이
	private Map<LocalDate, Long> dailyTicketCreation;

	// 일별 유저 가입 추이
	private Map<LocalDate, Long> dailyUserSignup;

	// 일별 워크스페이스 생성 추이
	private Map<LocalDate, Long> dailyWorkspaceCreation;
}
