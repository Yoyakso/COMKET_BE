package com.yoyakso.comket.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.yoyakso.comket.admin.dto.response.FeatureUsageStatisticsResponse;
import com.yoyakso.comket.admin.dto.response.OperationStatisticsResponse;
import com.yoyakso.comket.admin.dto.response.UserBehaviorStatisticsResponse;
import com.yoyakso.comket.ai.entity.AiSummary;
import com.yoyakso.comket.ai.enums.SummaryType;
import com.yoyakso.comket.ai.repository.AiSummaryRepository;
import com.yoyakso.comket.billing.entity.WorkspacePlan;
import com.yoyakso.comket.billing.repository.WorkspacePlanRepository;
import com.yoyakso.comket.billing.service.BillingService;
import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.file.repository.fileRepository;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.repository.MemberRepository;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.project.repository.ProjectRepository;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.repository.TicketRepository;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

	private final WorkspaceRepository workspaceRepository;
	private final ProjectRepository projectRepository;
	private final MemberRepository memberRepository;
	private final TicketRepository ticketRepository;
	private final AiSummaryRepository aiSummaryRepository;
	private final fileRepository fileRepository;
	private final WorkspacePlanRepository workspacePlanRepository;
	private final BillingService billingService;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final ThreadMessageRepository threadMessageRepository;

	/**
	 * 운영 통계 조회
	 */
	public OperationStatisticsResponse getOperationStatistics() {
		// 전체 워크스페이스 수
		long totalWorkspaces = workspaceRepository.count();

		// 전체 프로젝트 수
		long totalProjects = projectRepository.count();

		// 전체 유저 수 (삭제되지 않은 유저)
		List<Member> allMembers = memberRepository.findAll();
		long totalMembers = allMembers.stream()
			.filter(member -> !member.getIsDeleted())
			.count();

		// 전체 티켓 수 (삭제되지 않은 티켓)
		List<Ticket> allTickets = ticketRepository.findAll();
		long totalTickets = allTickets.stream()
			.filter(ticket -> !ticket.isDeleted())
			.count();

		// 하루 평균 티켓 생성 수 (최근 30일)
		LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
		long ticketsLast30Days = allTickets.stream()
			.filter(ticket -> !ticket.isDeleted() && ticket.getCreatedAt().isAfter(thirtyDaysAgo))
			.count();
		double avgDailyTickets = (double)ticketsLast30Days / 30;

		// 하루 평균 유저 가입 수 (최근 30일)
		long membersLast30Days = allMembers.stream()
			.filter(member -> !member.getIsDeleted() && member.getCreatedAt().isAfter(thirtyDaysAgo))
			.count();
		double avgDailySignups = (double)membersLast30Days / 30;

		// 활성 이용자 수 (최근 30일 로그인)
		long activeUsers = allMembers.stream()
			.filter(member -> !member.getIsDeleted() && member.getUpdatedAt().isAfter(thirtyDaysAgo))
			.count();

		// 이탈율 계산 (1 - 활성 사용자 / 전체 사용자)
		double churnRate = totalMembers > 0 ? 1 - ((double)activeUsers / totalMembers) : 0;

		// 전체 워크스페이스 사용 요금 계산
		long totalWorkspaceUsageFee = calculateTotalWorkspaceUsageFee();

		// 일별 티켓 생성 추이 (최근 7일)
		Map<LocalDate, Long> dailyTicketCreation = calculateDailyTicketCreation();

		// 일별 유저 가입 추이 (최근 7일)
		Map<LocalDate, Long> dailyUserSignup = calculateDailyUserSignup();

		// 일별 워크스페이스 생성 추이 (최근 7일)
		Map<LocalDate, Long> dailyWorkspaceCreation = calculateDailyWorkspaceCreation();

		return OperationStatisticsResponse.builder()
			.totalWorkspaces(totalWorkspaces)
			.totalProjects(totalProjects)
			.totalMembers(totalMembers)
			.totalTickets(totalTickets)
			.avgDailyTickets(avgDailyTickets)
			.avgDailySignups(avgDailySignups)
			.activeUsers(activeUsers)
			.churnRate(churnRate)
			.totalWorkspaceUsageFee(totalWorkspaceUsageFee)
			.dailyTicketCreation(dailyTicketCreation)
			.dailyUserSignup(dailyUserSignup)
			.dailyWorkspaceCreation(dailyWorkspaceCreation)
			.build();
	}

	/**
	 * 전체 워크스페이스 사용 요금 계산
	 */
	private long calculateTotalWorkspaceUsageFee() {
		List<WorkspacePlan> allWorkspacePlans = workspacePlanRepository.findAll();
		long totalFee = 0;

		for (WorkspacePlan plan : allWorkspacePlans) {
			// 워크스페이스의 활성 멤버 수 계산
			int memberCount = billingService.countActiveWorkspaceMembers(plan.getWorkspace().getId());

			// 월 비용 계산
			int monthlyCost = plan.calculateMonthlyCost(memberCount);

			// 총 비용에 추가
			totalFee += monthlyCost;
		}

		return totalFee;
	}

	/**
	 * 일별 티켓 생성 추이 계산 (최근 7일)
	 */
	private Map<LocalDate, Long> calculateDailyTicketCreation() {
		Map<LocalDate, Long> dailyTicketCreation = new HashMap<>();
		LocalDate today = LocalDate.now();

		// 최근 7일 데이터 초기화
		for (int i = 6; i >= 0; i--) {
			LocalDate date = today.minusDays(i);
			dailyTicketCreation.put(date, 0L);
		}

		// 티켓 데이터 집계
		List<Ticket> allTickets = ticketRepository.findAll();
		LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();

		Map<LocalDate, Long> ticketCounts = allTickets.stream()
			.filter(ticket -> !ticket.isDeleted() && ticket.getCreatedAt().isAfter(sevenDaysAgo))
			.collect(Collectors.groupingBy(
				ticket -> ticket.getCreatedAt().toLocalDate(),
				Collectors.counting()
			));

		// 집계 데이터 병합
		dailyTicketCreation.putAll(ticketCounts);

		return dailyTicketCreation;
	}

	/**
	 * 일별 유저 가입 추이 계산 (최근 7일)
	 */
	private Map<LocalDate, Long> calculateDailyUserSignup() {
		Map<LocalDate, Long> dailyUserSignup = new HashMap<>();
		LocalDate today = LocalDate.now();

		// 최근 7일 데이터 초기화
		for (int i = 6; i >= 0; i--) {
			LocalDate date = today.minusDays(i);
			dailyUserSignup.put(date, 0L);
		}

		// 유저 데이터 집계
		List<Member> allMembers = memberRepository.findAll();
		LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();

		Map<LocalDate, Long> userCounts = allMembers.stream()
			.filter(member -> !member.getIsDeleted() && member.getCreatedAt().isAfter(sevenDaysAgo))
			.collect(Collectors.groupingBy(
				member -> member.getCreatedAt().toLocalDate(),
				Collectors.counting()
			));

		// 집계 데이터 병합
		dailyUserSignup.putAll(userCounts);

		return dailyUserSignup;
	}

	/**
	 * 일별 워크스페이스 생성 추이 계산 (최근 7일)
	 */
	private Map<LocalDate, Long> calculateDailyWorkspaceCreation() {
		Map<LocalDate, Long> dailyWorkspaceCreation = new HashMap<>();
		LocalDate today = LocalDate.now();

		// 최근 7일 데이터 초기화 (데이터가 없는 경우 0으로 표시)
		for (int i = 6; i >= 0; i--) {
			LocalDate date = today.minusDays(i);
			dailyWorkspaceCreation.put(date, 0L);
		}

		// 워크스페이스 데이터 집계
		List<Workspace> allWorkspaces = workspaceRepository.findAll();
		LocalDateTime sevenDaysAgo = today.minusDays(6).atStartOfDay();

		Map<LocalDate, Long> workspaceCounts = allWorkspaces.stream()
			.filter(workspace -> workspace.getCreatedAt().isAfter(sevenDaysAgo))
			.collect(Collectors.groupingBy(
				workspace -> workspace.getCreatedAt().toLocalDate(),
				Collectors.counting()
			));

		// 집계 데이터 병합
		dailyWorkspaceCreation.putAll(workspaceCounts);

		return dailyWorkspaceCreation;
	}

	/**
	 * 사용자 행동 분석 조회
	 */
	public UserBehaviorStatisticsResponse getUserBehaviorStatistics() {
		// 활성 워크스페이스 top 10 (실제 활동량 기준)
		List<Workspace> allWorkspaces = workspaceRepository.findAll();
		List<Map<String, Object>> topWorkspaces = new ArrayList<>();
		List<Ticket> allTickets = ticketRepository.findAll();

		// 워크스페이스별 활동량 계산 (티켓 수 기준)
		List<Map.Entry<Workspace, Long>> workspaceActivity = new ArrayList<>();
		for (Workspace workspace : allWorkspaces) {
			// 워크스페이스에 속한 프로젝트 찾기
			List<Project> projects = projectRepository.findAllByWorkspaceAndState(workspace, ProjectState.ACTIVE);

			// 프로젝트에 속한 티켓 수 계산
			long ticketCount = 0;
			for (Project project : projects) {
				ticketCount += allTickets.stream()
					.filter(ticket -> !ticket.isDeleted() && ticket.getProject() != null && ticket.getProject().getId() == project.getId())
					.count();
			}

			workspaceActivity.add(Map.entry(workspace, ticketCount));
		}

		// 티켓 수 기준으로 정렬 (내림차순)
		workspaceActivity.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

		// 상위 10개 워크스페이스 추출
		for (int i = 0; i < Math.min(10, workspaceActivity.size()); i++) {
			Workspace workspace = workspaceActivity.get(i).getKey();
			long ticketCount = workspaceActivity.get(i).getValue();

			// 워크스페이스 멤버 수 계산
			int memberCount = workspaceMemberRepository.findByWorkspaceId(workspace.getId()).size();

			Map<String, Object> workspaceData = new HashMap<>();
			workspaceData.put("id", workspace.getId());
			workspaceData.put("name", workspace.getName());
			workspaceData.put("memberCount", memberCount);
			workspaceData.put("ticketCount", ticketCount);
			topWorkspaces.add(workspaceData);
		}

		// 티켓 템플릿 사용 비중
		Map<String, Long> ticketTypeDistribution = allTickets.stream()
			.filter(ticket -> !ticket.isDeleted())
			.collect(Collectors.groupingBy(
				ticket -> ticket.getType().getType(),
				Collectors.counting()
			));

		// 티켓 상태 분포
		Map<String, Long> ticketStateDistribution = allTickets.stream()
			.filter(ticket -> !ticket.isDeleted())
			.collect(Collectors.groupingBy(
				ticket -> ticket.getState().name(),
				Collectors.counting()
			));

		// 스레드 당 평균 메세지 수 계산
		double avgMessagesPerThread = 0.0;
		long totalMessages = threadMessageRepository.count();
		long totalThreads = allTickets.stream().filter(ticket -> !ticket.isDeleted()).count();

		if (totalThreads > 0) {
			avgMessagesPerThread = (double) totalMessages / totalThreads;
		}

		// 1인당 평균 담당 티켓 수 계산
		double avgTicketsPerMember = 0.0;
		long totalAssignments = 0;
		List<Member> allMembers = memberRepository.findAll();
		long activeMembers = allMembers.stream().filter(member -> !member.getIsDeleted()).count();

		// 각 티켓의 담당자 수 합산
		for (Ticket ticket : allTickets) {
			if (!ticket.isDeleted()) {
				totalAssignments += ticket.getAssignees().size();
			}
		}

		if (activeMembers > 0) {
			avgTicketsPerMember = (double) totalAssignments / activeMembers;
		}

		return UserBehaviorStatisticsResponse.builder()
			.topWorkspaces(topWorkspaces)
			.ticketTypeDistribution(ticketTypeDistribution)
			.ticketStateDistribution(ticketStateDistribution)
			.avgMessagesPerThread(avgMessagesPerThread)
			.avgTicketsPerMember(avgTicketsPerMember)
			.build();
	}

	/**
	 * 기능 사용률 분석 조회
	 */
	public FeatureUsageStatisticsResponse getFeatureUsageStatistics() {
		// AI 요약 기능 사용 횟수 및 데이터 조회
		List<AiSummary> allAiSummaries = aiSummaryRepository.findAll();
		long aiSummaryCount = allAiSummaries.size();

		// AI 요약 기능 일별 사용 횟수
		Map<LocalDate, Long> aiSummaryDailyUsage = allAiSummaries.stream()
			.collect(Collectors.groupingBy(
				summary -> summary.getCreateTime().toLocalDate(),
				Collectors.counting()
			));

		// AI 요약 직군별 사용 횟수
		Map<String, Long> aiSummaryTypeDistribution = allAiSummaries.stream()
			.filter(summary -> summary.getSummaryType() != null)
			.collect(Collectors.groupingBy(
				summary -> {
					SummaryType type = summary.getSummaryType();
					switch (type) {
						case DEVELOPER:
							return "개발자";
						case PROJECT_MANAGER:
							return "기획자";
						case DESIGNER:
							return "디자이너";
						case DATA_ANALYST:
							return "데이터 분석가";
						default:
							return "기본";
					}
				},
				Collectors.counting()
			));

		// 파일 업로드 빈도 및 용량
		long fileCount = fileRepository.count();

		// 파일 총 용량 (파일 크기의 합계)
		long totalFileSize = fileRepository.findAll().stream()
			.mapToLong(File::getFileSize)
			.sum();

		return FeatureUsageStatisticsResponse.builder()
			.aiSummaryCount(aiSummaryCount)
			.aiSummaryDailyUsage(aiSummaryDailyUsage)
			.aiSummaryTypeDistribution(aiSummaryTypeDistribution)
			.fileCount(fileCount)
			.totalFileSize(totalFileSize)
			.build();
	}
}
