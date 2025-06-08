package com.yoyakso.comket.billing.service;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.billing.entity.CreditCard;
import com.yoyakso.comket.billing.entity.WorkspaceBilling;
import com.yoyakso.comket.billing.enums.BillingPlan;
import com.yoyakso.comket.billing.repository.CreditCardRepository;
import com.yoyakso.comket.billing.repository.WorkspaceBillingRepository;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BillingService {

	private final WorkspaceBillingRepository workspaceBillingRepository;
	private final CreditCardRepository creditCardRepository;
	private final WorkspaceMemberService workspaceMemberService;
	private final WorkspaceMemberHistoryService workspaceMemberHistoryService;

	/**
	 * 새 워크스페이스에 대한 빌링 초기화
	 */
	public WorkspaceBilling initializeWorkspaceBilling(Workspace workspace) {
		// 워크스페이스의 활성 멤버 수 계산
		int memberCount = countActiveWorkspaceMembers(workspace.getId());

		// 멤버 수에 따른 적절한 요금제 결정
		BillingPlan plan = BillingPlan.getPlanForMemberCount(memberCount);

		// 워크스페이스 빌링 생성 및 저장
		WorkspaceBilling workspaceBilling = WorkspaceBilling.builder()
			.workspace(workspace)
			.currentPlan(plan)
			.memberCount(memberCount)
			.build();

		return workspaceBillingRepository.save(workspaceBilling);
	}

	/**
	 * 워크스페이스의 빌링 정보 조회
	 */
	public WorkspaceBilling getWorkspaceBilling(Long workspaceId) {
		return workspaceBillingRepository.findByWorkspaceId(workspaceId)
			.orElseThrow(() -> new CustomException("BILLING_NOT_FOUND", "워크스페이스의 빌링 정보를 찾을 수 없습니다."));
	}

	/**
	 * 워크스페이스에 신용 카드 등록
	 */
	public CreditCard registerCreditCard(Long workspaceId, CreditCard creditCard) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		// 신용 카드 저장
		CreditCard savedCreditCard = creditCardRepository.save(creditCard);

		// 워크스페이스 빌링에 신용 카드 정보 업데이트
		workspaceBilling.setCreditCard(savedCreditCard);
		workspaceBillingRepository.save(workspaceBilling);

		return savedCreditCard;
	}

	/**
	 * 워크스페이스의 신용 카드 정보 조회
	 */
	public CreditCard getCreditCard(Long workspaceId) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		if (workspaceBilling.getCreditCard() == null) {
			throw new CustomException("CREDIT_CARD_NOT_FOUND", "등록된 신용 카드 정보가 없습니다.");
		}

		return workspaceBilling.getCreditCard();
	}

	/**
	 * 워크스페이스의 신용 카드 정보 업데이트
	 */
	public CreditCard updateCreditCard(Long workspaceId, CreditCard creditCard) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		// 기존 신용 카드 정보 확인
		if (workspaceBilling.getCreditCard() == null) {
			throw new CustomException("CREDIT_CARD_NOT_FOUND", "업데이트할 신용 카드 정보가 없습니다.");
		}

		// 기존 ID 유지
		creditCard.setId(workspaceBilling.getCreditCard().getId());

		// 신용 카드 정보 업데이트
		CreditCard updatedCreditCard = creditCardRepository.save(creditCard);

		// 워크스페이스 빌링에 업데이트된 신용 카드 정보 설정
		workspaceBilling.setCreditCard(updatedCreditCard);
		workspaceBillingRepository.save(workspaceBilling);

		return updatedCreditCard;
	}

	/**
	 * 워크스페이스의 요금제 변경
	 */
	public WorkspaceBilling changeBillingPlan(Long workspaceId, BillingPlan newPlan) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		// 새 요금제에 신용 카드가 필요한지 확인
		if (newPlan != BillingPlan.BASIC && workspaceBilling.getCreditCard() == null) {
			throw new CustomException("CREDIT_CARD_REQUIRED", "유료 요금제로 변경하려면 신용 카드 정보가 필요합니다.");
		}

		// 현재 활성 멤버 수 계산하여 업데이트
		int currentMemberCount = countActiveWorkspaceMembers(workspaceId);
		workspaceBilling.setMemberCount(currentMemberCount);

		// 요금제 업데이트
		workspaceBilling.setCurrentPlan(newPlan);
		return workspaceBillingRepository.save(workspaceBilling);
	}

	/**
	 * 새 멤버 추가 시 요금제 변경이 필요한지 확인
	 */
	public boolean checkIfPlanChangeRequired(Long workspaceId, int newMembersCount) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);
		int currentMemberCount = workspaceBilling.getMemberCount();
		int newTotalCount = currentMemberCount + newMembersCount;

		BillingPlan currentPlan = workspaceBilling.getCurrentPlan();
		BillingPlan requiredPlan = BillingPlan.getPlanForMemberCount(newTotalCount);

		return currentPlan != requiredPlan;
	}

	/**
	 * 멤버 추가 또는 제거 시 멤버 수와 요금제 업데이트
	 */
	public WorkspaceBilling updateMemberCountAndPlan(Long workspaceId) {
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		// 활성 멤버 수 계산
		int memberCount = countActiveWorkspaceMembers(workspaceId);
		workspaceBilling.setMemberCount(memberCount);

		// 적절한 요금제 결정
		BillingPlan requiredPlan = BillingPlan.getPlanForMemberCount(memberCount);

		// 요금제 변경 필요 여부 확인
		if (workspaceBilling.getCurrentPlan() != requiredPlan) {
			// 유료 요금제로 변경 시 신용 카드 정보 확인
			if (requiredPlan != BillingPlan.BASIC && workspaceBilling.getCreditCard() == null) {
				throw new CustomException("CREDIT_CARD_REQUIRED", "유료 요금제로 변경하려면 신용 카드 정보가 필요합니다.");
			}
			workspaceBilling.setCurrentPlan(requiredPlan);
		}

		return workspaceBillingRepository.save(workspaceBilling);
	}

	/**
	 * 워크스페이스의 월별 멤버 수 히스토리 조회
	 */
	public Map<String, Integer> getMemberCountHistory(Long workspaceId) {
		// WorkspaceMemberHistory 테이블에서 히스토리 데이터 조회
		Map<String, Integer> history = workspaceMemberHistoryService.getMemberCountHistoryMap(workspaceId);

		// 현재 월 정보 가져오기
		YearMonth currentMonth = YearMonth.now();
		String currentMonthStr = currentMonth.toString();

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
		// WorkspaceMemberHistory 테이블에서 멤버 수 히스토리 조회
		Map<String, Integer> memberCountHistory = getMemberCountHistory(workspaceId);
		Map<String, Integer> billingHistory = new HashMap<>();
		WorkspaceBilling workspaceBilling = getWorkspaceBilling(workspaceId);

		// 현재 월 정보 가져오기
		YearMonth currentMonth = YearMonth.now();
		String currentMonthStr = currentMonth.toString();

		// 멤버 수에 기반하여 각 월별 빌링 금액 계산
		for (Map.Entry<String, Integer> entry : memberCountHistory.entrySet()) {
			String yearMonth = entry.getKey();
			int memberCount = entry.getValue();

			// 멤버 수에 따른 적절한 요금제 결정
			BillingPlan plan = BillingPlan.getPlanForMemberCount(memberCount);

			// 빌링 금액 계산
			int billingAmount = plan.getPricePerMember() * memberCount;
			billingHistory.put(yearMonth, billingAmount);
		}

		// 히스토리가 없는 경우 현재 빌링 금액 사용
		if (billingHistory.isEmpty()) {
			billingHistory.put(currentMonthStr, workspaceBilling.calculateMonthlyCost());
		}

		return billingHistory;
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
