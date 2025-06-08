package com.yoyakso.comket.billing.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.billing.entity.WorkspaceMemberHistory;
import com.yoyakso.comket.billing.repository.WorkspaceMemberHistoryRepository;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkspaceMemberHistoryService {

	private final WorkspaceMemberHistoryRepository workspaceMemberHistoryRepository;
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMemberService workspaceMemberService;

	// 매월 20일 마다 히스토리 테이블 동작하도록 진행
	@Scheduled(cron = "0 0 0 20 * ?")
	public void recordMonthlyWorkspaceMemberCounts() {
		// 현재 년도와 월 가져오기
		YearMonth currentMonth = YearMonth.now();
		int year = currentMonth.getYear();
		int month = currentMonth.getMonthValue();

		// 모든 활성 워크스페이스 가져오기
		List<Workspace> workspaces = workspaceRepository.findAll();

		for (Workspace workspace : workspaces) {
			try {
				int memberCount = countActiveWorkspaceMembers(workspace.getId());

				// 히스토리 기록 생성 및 저장
				WorkspaceMemberHistory history = WorkspaceMemberHistory.builder()
					.workspace(workspace)
					.year(year)
					.month(month)
					.memberCount(memberCount)
					.build();

				workspaceMemberHistoryRepository.save(history);
			} catch (Exception e) {
				// 예외 발생 시 로깅하지 않음
			}
		}
	}

	/**
	 * 특정 워크스페이스의 멤버 수 기록
	 */
	public WorkspaceMemberHistory recordWorkspaceMemberCount(Long workspaceId) {
		Workspace workspace = workspaceRepository.findById(workspaceId)
			.orElseThrow(() -> new RuntimeException("워크스페이스를 찾을 수 없습니다: " + workspaceId));

		int memberCount = countActiveWorkspaceMembers(workspaceId);
		YearMonth currentMonth = YearMonth.now();

		WorkspaceMemberHistory history = WorkspaceMemberHistory.builder()
			.workspace(workspace)
			.year(currentMonth.getYear())
			.month(currentMonth.getMonthValue())
			.memberCount(memberCount)
			.build();

		return workspaceMemberHistoryRepository.save(history);
	}

	/**
	 * 워크스페이스의 멤버 수 히스토리 조회
	 */
	public List<WorkspaceMemberHistory> getMemberCountHistory(Long workspaceId) {
		return workspaceMemberHistoryRepository.findByWorkspaceIdOrderByYearMonthDesc(workspaceId);
	}

	/**
	 * 워크스페이스의 멤버 수 히스토리 맵 조회 (최근 12개월)
	 */
	public Map<String, Integer> getMemberCountHistoryMap(Long workspaceId) {
		List<WorkspaceMemberHistory> historyList = getMemberCountHistory(workspaceId);

		// YearMonth 문자열을 키로 하는 맵으로 변환
		return historyList.stream()
			.limit(12) // 최근 12개월로 제한
			.collect(Collectors.toMap(
				history -> history.getYearMonth().toString(),
				WorkspaceMemberHistory::getMemberCount,
				(existing, replacement) -> existing // 중복 시 첫 번째 값 유지
			));
	}

	/**
	 * 워크스페이스의 활성 멤버 수 계산
	 */
	private int countActiveWorkspaceMembers(Long workspaceId) {
		List<WorkspaceMember> members = workspaceMemberService.getWorkspaceMembersByWorkspaceId(workspaceId);
		return (int)members.stream()
			.filter(member -> member.getState() == WorkspaceMemberState.ACTIVE)
			.count();
	}
}
