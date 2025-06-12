package com.yoyakso.comket.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.billing.entity.Payment;
import com.yoyakso.comket.billing.service.PaymentService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.service.WorkspaceService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
	private final PaymentService paymentService;
	private final MemberService memberService;
	private final WorkspaceService workspaceService;

	// 결제 정보 저장 API
	@PostMapping("/register")
	@Operation(summary = "결제 정보 등록/갱신", description = "정기 결제를 위한 결제 정보를 등록합니다.")
	public ResponseEntity<Payment> registerPayment(
		@PathVariable Long workspaceId,
		@RequestParam String impUid
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();
		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);
		// 관리자 권한 확인
		workspaceService.validateAdminPermission(authenticatedMember, workspace);

		// 결제 검증
		paymentService.verifyPayment(impUid);

		// 결제 정보 저장
		Payment payment = paymentService.savePayment(impUid, workspace, authenticatedMember);

		return ResponseEntity.ok(payment);
	}

	// 결제 등록 유무 확인 API
	@GetMapping("/status")
	@Operation(summary = "결제 등록 유무 확인", description = "워크스페이스에 결제 정보가 등록되어 있는지 확인합니다.")
	public ResponseEntity<Boolean> checkPaymentStatus(
		@PathVariable Long workspaceId
	) {
		// 인증된 사용자 확인
		Member authenticatedMember = memberService.getAuthenticatedMember();
		// 워크스페이스 접근 권한 확인
		Workspace workspace = workspaceService.getWorkspaceById(workspaceId, authenticatedMember);

		// 결제 등록 유무 확인
		boolean isRegistered = paymentService.isPaymentRegistered(workspace);

		return ResponseEntity.ok(isRegistered);
	}
}
