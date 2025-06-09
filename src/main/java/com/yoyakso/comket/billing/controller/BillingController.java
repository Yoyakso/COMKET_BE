package com.yoyakso.comket.billing.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.billing.dto.request.BillingPlanChangeRequest;
import com.yoyakso.comket.billing.dto.request.CreditCardRegisterRequest;
import com.yoyakso.comket.billing.dto.request.CreditCardUpdateRequest;
import com.yoyakso.comket.billing.dto.response.BillingStatusResponse;
import com.yoyakso.comket.billing.dto.response.CreditCardResponse;
import com.yoyakso.comket.billing.entity.CreditCard;
import com.yoyakso.comket.billing.entity.WorkspacePlan;
import com.yoyakso.comket.billing.mapper.BillingMapper;
import com.yoyakso.comket.billing.service.BillingService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/billing")
@RequiredArgsConstructor
public class BillingController {

	private final BillingService billingService;
	private final WorkspaceService workspaceService;
	private final MemberService memberService;
	private final BillingMapper billingMapper;

	@GetMapping
	@Operation(
		summary = "워크스페이스 빌링 정보 조회 API",
		description = "워크스페이스의 빌링 정보를 조회하는 API\n\n" +
			"응답에 포함된 금액 필드 설명:\n" +
			"- confirmedAmount: DB에 저장된 워크스페이스 빌링 설정(멤버 수와 요금제)을 기준으로 계산된 확정 금액\n" +
			"- estimatedAmount: 현재 실제 활성 멤버 수와 그에 따른 요금제를 기준으로 계산된 이번 달 예상 금액\n" +
			"- displayAmount: 날짜에 따라 표시할 금액 (20일 이전: 예상 금액, 20일 이후: 확정 금액)\n" +
			"- 멤버 수가 변경되었지만 아직 빌링 정보가 업데이트되지 않은 경우 확정 금액과 예상 금액이 다를 수 있습니다."
	)
	public ResponseEntity<BillingStatusResponse> getBillingStatus(@PathVariable Long workspaceId) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 요금제 정보 조회
		WorkspacePlan workspacePlan = billingService.getWorkspacePlan(workspaceId);

		// 멤버 수 히스토리 조회
		Map<String, Integer> memberCountHistory = billingService.getMemberCountHistory(workspaceId);

		// 빌링 금액 히스토리 조회
		Map<String, Integer> billingAmountHistory = billingService.getBillingAmountHistory(workspaceId);

		// 현재 활성 멤버 수 계산
		int memberCount = billingService.countActiveWorkspaceMembers(workspaceId);

		// 응답 생성
		return ResponseEntity.ok(billingMapper.toBillingStatusResponse(
			workspacePlan, memberCountHistory, billingAmountHistory, memberCount));
	}

	@PostMapping("/credit-card")
	@Operation(summary = "신용 카드 등록 API", description = "워크스페이스에 신용 카드를 등록하는 API")
	public ResponseEntity<Void> registerCreditCard(
		@PathVariable Long workspaceId,
		@Valid @RequestBody CreditCardRegisterRequest request
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 신용 카드 생성 및 등록
		billingService.registerCreditCard(workspaceId, billingMapper.toEntity(request));

		return ResponseEntity.ok().build();
	}

	@GetMapping("/credit-card")
	@Operation(summary = "신용 카드 조회 API", description = "워크스페이스에 등록된 신용 카드 정보를 조회하는 API")
	public ResponseEntity<CreditCardResponse> getCreditCard(
		@PathVariable Long workspaceId
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 신용 카드 정보 조회
		CreditCard creditCard = billingService.getCreditCard(workspaceId);

		// 응답 생성
		return ResponseEntity.ok(billingMapper.toCreditCardResponse(creditCard));
	}

	@PutMapping("/credit-card")
	@Operation(summary = "신용 카드 수정 API", description = "워크스페이스에 등록된 신용 카드 정보를 수정하는 API")
	public ResponseEntity<CreditCardResponse> updateCreditCard(
		@PathVariable Long workspaceId,
		@Valid @RequestBody CreditCardUpdateRequest request
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 신용 카드 정보 업데이트
		CreditCard updatedCreditCard = billingService.updateCreditCard(workspaceId, billingMapper.toEntity(request));

		// 응답 생성
		return ResponseEntity.ok(billingMapper.toCreditCardResponse(updatedCreditCard));
	}

	@PostMapping("/admin/record")
	@Operation(summary = "워크스페이스 빌링 히스토리 수동 기록 API", description = "워크스페이스 빌링 데이터를 수동으로 기록하는 API")
	public ResponseEntity<Void> recordWorkspaceBillingHistory(@PathVariable Long workspaceId) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 워크스페이스 빌링 히스토리 기록
		billingService.recordMonthlyWorkspaceBillingData();

		return ResponseEntity.ok().build();
	}

	@PutMapping("/plan")
	@Operation(
		summary = "요금제 변경 API",
		description = "워크스페이스의 요금제를 변경하는 API\n\n" +
			"응답에 포함된 금액 필드 설명:\n" +
			"- confirmedAmount: DB에 저장된 워크스페이스 빌링 설정(멤버 수와 요금제)을 기준으로 계산된 확정 금액\n" +
			"- estimatedAmount: 현재 실제 활성 멤버 수와 그에 따른 요금제를 기준으로 계산된 이번 달 예상 금액\n" +
			"- displayAmount: 날짜에 따라 표시할 금액 (20일 이전: 예상 금액, 20일 이후: 확정 금액)\n" +
			"- 멤버 수가 변경되었지만 아직 빌링 정보가 업데이트되지 않은 경우 확정 금액과 예상 금액이 다를 수 있습니다."
	)
	public ResponseEntity<BillingStatusResponse> changeBillingPlan(
		@PathVariable Long workspaceId,
		@Valid @RequestBody BillingPlanChangeRequest request
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();

		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 요금제 변경
		WorkspacePlan workspacePlan = billingService.changeBillingPlan(workspaceId, request.getPlan());

		// 멤버 수 히스토리 조회
		var memberCountHistory = billingService.getMemberCountHistory(workspaceId);

		// 빌링 금액 히스토리 조회
		var billingAmountHistory = billingService.getBillingAmountHistory(workspaceId);

		// 현재 활성 멤버 수 계산
		int memberCount = billingService.countActiveWorkspaceMembers(workspaceId);

		// 응답 생성
		return ResponseEntity.ok(billingMapper.toBillingStatusResponse(
			workspacePlan, memberCountHistory, billingAmountHistory, memberCount));
	}
}
