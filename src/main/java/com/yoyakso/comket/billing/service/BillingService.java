package com.yoyakso.comket.billing.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.billing.entity.WorkspaceBilling;
import com.yoyakso.comket.billing.entity.WorkspacePlan;
import com.yoyakso.comket.billing.enums.BillingPlan;
import com.yoyakso.comket.billing.repository.WorkspaceBillingRepository;
import com.yoyakso.comket.billing.repository.WorkspacePlanRepository;
import com.yoyakso.comket.billing.service.PaymentService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.event.WorkspaceCreatedEvent;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BillingService {

	private final WorkspaceBillingRepository workspaceBillingRepository;
	private final WorkspacePlanRepository workspacePlanRepository;
	private final WorkspaceMemberService workspaceMemberService;
	private final WorkspaceRepository workspaceRepository;
	private final PaymentService paymentService;

	/**
	 * 워크스페이스 생성 이벤트를 수신하여 요금제 초기화
	 */
	@EventListener
	public void handleWorkspaceCreatedEvent(WorkspaceCreatedEvent event) {
		initializeWorkspacePlan(event.getWorkspace());
	}

	/**
	 * 새 워크스페이스에 대한 요금제 초기화
	 */
	public WorkspacePlan initializeWorkspacePlan(Workspace workspace) {
		// 워크스페이스의 활성 멤버 수 계산
		int memberCount = countActiveWorkspaceMembers(workspace.getId());

		// 멤버 수에 따른 적절한 요금제 결정
		BillingPlan plan = BillingPlan.getPlanForMemberCount(memberCount);

		// 워크스페이스 요금제 생성 및 저장
		WorkspacePlan workspacePlan = WorkspacePlan.builder()
			.workspace(workspace)
			.currentPlan(plan)
			.build();

		return workspacePlanRepository.save(workspacePlan);
	}

	/**
	 * 워크스페이스의 요금제 정보 조회
	 */
	public WorkspacePlan getWorkspacePlan(Long workspaceId) {
		return workspacePlanRepository.findByWorkspaceId(workspaceId)
			.orElseThrow(() -> new CustomException("PLAN_NOT_FOUND", "워크스페이스의 요금제 정보를 찾을 수 없습니다."));
	}

	/**
	 * 워크스페이스의 요금제 업데이트
	 */
	public WorkspacePlan updateWorkspacePlan(Long workspaceId, int memberCountChange) {
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);

		// 현재 멤버 수 계산
		int newMemberCount;
		if (memberCountChange != 0) {
			// 이벤트에서 전달된 변경 수 적용
			int currentMemberCount = countActiveWorkspaceMembers(workspaceId);
			newMemberCount = currentMemberCount + memberCountChange;
		} else {
			// 직접 계산 (멤버 수 동기화 목적)
			newMemberCount = countActiveWorkspaceMembers(workspaceId);
		}

		// 적절한 요금제 결정
		BillingPlan requiredPlan = BillingPlan.getPlanForMemberCount(newMemberCount);

		// 요금제 변경 필요 여부 확인
		if (workspacePlan.getCurrentPlan() != requiredPlan) {
			// 유료 요금제로 변경 시 결제 정보 확인
			if (requiredPlan != BillingPlan.BASIC) {
				// 결제 정보가 등록되어 있는지 확인
				boolean isPaymentRegistered = paymentService.isPaymentRegistered(workspacePlan.getWorkspace());
				if (!isPaymentRegistered) {
					throw new CustomException("PAYMENT_REQUIRED", "유료 요금제로 변경하려면 결제 정보가 필요합니다.");
				}
			}
			workspacePlan.setCurrentPlan(requiredPlan);
		}

		return workspacePlanRepository.save(workspacePlan);
	}


	/**
	 * 워크스페이스의 요금제 변경
	 */
	public WorkspacePlan changeBillingPlan(Long workspaceId, BillingPlan newPlan) {
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);
		Workspace workspace = workspacePlan.getWorkspace();

		// 새 요금제에 결제 정보가 필요한지 확인
		if (newPlan != BillingPlan.BASIC) {
			// 결제 정보가 등록되어 있는지 확인
			boolean isPaymentRegistered = paymentService.isPaymentRegistered(workspace);
			if (!isPaymentRegistered) {
				throw new CustomException("PAYMENT_REQUIRED", "유료 요금제로 변경하려면 결제 정보가 필요합니다.");
			}
		}

		// 요금제 업데이트
		workspacePlan.setCurrentPlan(newPlan);
		return workspacePlanRepository.save(workspacePlan);
	}

	/**
	 * 새 멤버 추가 시 요금제 변경이 필요한지 확인
	 */
	public boolean checkIfPlanChangeRequired(Long workspaceId, int newMembersCount) {
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);
		int currentMemberCount = countActiveWorkspaceMembers(workspaceId);
		int newTotalCount = currentMemberCount + newMembersCount;

		BillingPlan currentPlan = workspacePlan.getCurrentPlan();
		BillingPlan requiredPlan = BillingPlan.getPlanForMemberCount(newTotalCount);

		return currentPlan != requiredPlan;
	}

	/**
	 * 새 멤버 추가 시 요금제 변경 및 신용 카드 필요 여부 확인
	 * 요금제 변경이 필요하고 신용 카드가 필요한 경우 예외 발생
	 */
	public void validatePlanChangeForNewMembers(Long workspaceId, int newMembersCount) {
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);
		int currentMemberCount = countActiveWorkspaceMembers(workspaceId);
		int newTotalCount = currentMemberCount + newMembersCount;

		BillingPlan currentPlan = workspacePlan.getCurrentPlan();
		BillingPlan requiredPlan = BillingPlan.getPlanForMemberCount(newTotalCount);

		// 요금제 변경이 필요한 경우
		if (currentPlan != requiredPlan) {
			// 유료 요금제로 변경 시 결제 정보 확인
			if (requiredPlan != BillingPlan.BASIC) {
				// 결제 정보가 등록되어 있는지 확인
				boolean isPaymentRegistered = paymentService.isPaymentRegistered(workspacePlan.getWorkspace());
				if (!isPaymentRegistered) {
					throw new CustomException("PAYMENT_REQUIRED",
						"유료 요금제(" + requiredPlan.getDisplayName() + ")로 변경하려면 결제 정보가 필요합니다.");
				}
			}

			throw new CustomException("PLAN_CHANGE_REQUIRED",
				"멤버 초대 시 요금제 변경이 필요합니다. 현재 요금제: " + currentPlan.getDisplayName() +
					", 필요 요금제: " + requiredPlan.getDisplayName());
		}
	}

	// 매월 20일마다 히스토리 테이블 동작하도록 진행
	@Scheduled(cron = "0 0 0 20 * ?")
	public void recordMonthlyWorkspaceBillingData() {
		// 이전 달 정보 가져오기 (현재 달이 아닌 이전 달 데이터를 확정)
		YearMonth previousMonth = YearMonth.now().minusMonths(1);
		int year = previousMonth.getYear();
		int month = previousMonth.getMonthValue();

		// 모든 활성 워크스페이스 가져오기
		List<Workspace> workspaces = workspaceRepository.findAll();

		for (Workspace workspace : workspaces) {
			try {
				// 현재 워크스페이스 요금제 정보 가져오기
				WorkspacePlan workspacePlan = getWorkspacePlan(workspace.getId());

				// 이전 달에 대한 히스토리가 이미 있는지 확인
				Optional<WorkspaceBilling> existingHistory =
					workspaceBillingRepository.findByWorkspaceIdAndYearAndMonth(
						workspace.getId(), year, month);

				if (existingHistory.isEmpty()) {
					// 이전 달 멤버 수 계산 (현재 멤버 수 사용)
					int memberCount = countActiveWorkspaceMembers(workspace.getId());

					// 요금제 정보 가져오기
					BillingPlan plan = workspacePlan.getCurrentPlan();

					// 금액 계산
					int amount = plan.getPricePerMember() * memberCount;

					// 히스토리 기록 생성 및 저장
					WorkspaceBilling history = WorkspaceBilling.createForMonth(
						workspace, plan, memberCount, year, month, amount);

					workspaceBillingRepository.save(history);
				}
			} catch (Exception e) {
				// 예외 발생 시 로깅
			}
		}
	}

	/**
	 * 워크스페이스의 월별 멤버 수 히스토리 조회
	 */
	public Map<String, Integer> getMemberCountHistory(Long workspaceId) {
		// WorkspaceBilling 테이블에서 히스토리 데이터 조회
		List<WorkspaceBilling> historyList =
			workspaceBillingRepository.findHistoryByWorkspaceIdOrderByYearMonthDesc(workspaceId);

		Map<String, Integer> history = historyList.stream()
			.limit(12) // 최근 12개월로 제한
			.collect(Collectors.toMap(
				billing -> billing.getYearMonth().toString(),
				WorkspaceBilling::getMemberCount,
				(existing, replacement) -> existing // 중복 시 첫 번째 값 유지
			));

		// 현재 월 정보 가져오기
		YearMonth currentMonth = YearMonth.now();
		String currentMonthStr = currentMonth.toString();

		// 현재 워크스페이스 요금제 정보 가져오기
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);

		// 현재 월에 대해서는 현재 멤버 수로 업데이트 (실시간 데이터 사용)
		int currentMemberCount = countActiveWorkspaceMembers(workspaceId);

		// 현재 월 데이터 업데이트 또는 추가
		history.put(currentMonthStr, currentMemberCount);

		return history;
	}

	/**
	 * 워크스페이스의 월별 빌링 금액 히스토리 조회
	 */
	public Map<String, Integer> getBillingAmountHistory(Long workspaceId) {
		// WorkspaceBilling 테이블에서 히스토리 데이터 조회
		List<WorkspaceBilling> historyList =
			workspaceBillingRepository.findHistoryByWorkspaceIdOrderByYearMonthDesc(workspaceId);

		Map<String, Integer> billingHistory = historyList.stream()
			.limit(12) // 최근 12개월로 제한
			.collect(Collectors.toMap(
				billing -> billing.getYearMonth().toString(),
				billing -> billing.getAmount(), // 확정된 금액 사용
				(existing, replacement) -> existing // 중복 시 첫 번째 값 유지
			));

		// 현재 월 정보 가져오기
		YearMonth currentMonth = YearMonth.now();
		String currentMonthStr = currentMonth.toString();

		// 현재 워크스페이스 요금제 정보 가져오기
		WorkspacePlan workspacePlan = getWorkspacePlan(workspaceId);

		// 현재 멤버 수 계산
		int currentMemberCount = countActiveWorkspaceMembers(workspaceId);

		// 현재 월에 대해서는 현재 요금제로 계산 (실시간 데이터 사용)
		int currentBillingAmount = workspacePlan.calculateMonthlyCost(currentMemberCount);

		// 현재 월 데이터 업데이트 또는 추가
		billingHistory.put(currentMonthStr, currentBillingAmount);

		return billingHistory;
	}

	/**
	 * 워크스페이스의 활성 멤버 수 계산
	 */
	public int countActiveWorkspaceMembers(Long workspaceId) {
		List<WorkspaceMember> members = workspaceMemberService.getWorkspaceMembersByWorkspaceId(workspaceId);
		return (int)members.stream()
			.filter(member -> member.getState() == WorkspaceMemberState.ACTIVE)
			.count();
	}
}
