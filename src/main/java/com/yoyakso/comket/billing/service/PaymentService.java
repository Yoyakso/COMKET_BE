package com.yoyakso.comket.billing.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.yoyakso.comket.billing.entity.Payment;
import com.yoyakso.comket.billing.enums.PaymentStatus;
import com.yoyakso.comket.billing.repository.PaymentRepository;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final IamportClient iamportClient;

	// Verify payment with Iamport
	public void verifyPayment(String impUid) {
		// 결제 정보 조회
		try {
			IamportResponse<com.siot.IamportRestClient.response.Payment> payment = iamportClient.paymentByImpUid(
				impUid);
			if (!payment.getResponse().getStatus().equals(PaymentStatus.PAID.getKey())) {
				throw new RuntimeException("결제 상태가 'paid'가 아닙니다. 현재 상태: " + payment.getResponse().getStatus());
			}
		} catch (Exception e) {
			throw new CustomException("PAYMENT_VERIFICATION_FAILED", "결제 검증에 실패했습니다. impUid: " + impUid);
		}
	}

	/**
	 * 결제 정보를 저장하거나 업데이트합니다.
	 * 워크스페이스에 연결된 결제 정보가 이미 존재하는 경우 기존 결제 정보를 업데이트하고, 
	 * 그렇지 않은 경우 새로운 결제 정보를 생성합니다.
	 * 정기 결제용 등록이 되어 있는지는 impUid가 있는지 없는지로 판단합니다.
	 * 
	 * @param impUid 결제 고유 번호
	 * @param workspace 워크스페이스
	 * @param member 결제한 회원 (사용하지 않음)
	 * @return 저장된 결제 정보
	 */
	public Payment savePayment(String impUid, Workspace workspace, Member member) {
		// 워크스페이스에 연결된 기존 결제 정보 조회
		Optional<Payment> existingWorkspacePayment = paymentRepository.findByWorkspace(workspace);

		if (existingWorkspacePayment.isPresent()) {
			// 워크스페이스에 이미 결제 정보가 있으면 업데이트
			Payment payment = existingWorkspacePayment.get();
			payment.setImpUid(impUid); // impUid 업데이트
			return paymentRepository.save(payment);
		} else {
			// 새로운 결제 정보 생성
			Payment payment = Payment.builder()
				.impUid(impUid)
				.workspace(workspace)
				.build();
			return paymentRepository.save(payment);
		}
	}

	/**
	 * 워크스페이스에 결제 정보가 등록되어 있는지 확인합니다.
	 * 워크스페이스에 연결된 결제 정보가 존재하고, impUid가 null이 아닌 경우 true를 반환합니다.
	 * 
	 * @param workspace 확인할 워크스페이스
	 * @return 결제 정보 등록 여부
	 */
	@Transactional(readOnly = true)
	public boolean isPaymentRegistered(Workspace workspace) {
		Optional<Payment> payment = paymentRepository.findByWorkspace(workspace);
		return payment.isPresent() && payment.get().getImpUid() != null;
	}
}
