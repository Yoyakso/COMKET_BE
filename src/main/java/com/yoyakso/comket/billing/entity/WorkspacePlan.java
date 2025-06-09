package com.yoyakso.comket.billing.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.billing.enums.BillingPlan;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workspace_plan")
public class WorkspacePlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", nullable = false)
	private Workspace workspace;

	@NotNull
	@Enumerated(EnumType.STRING)
	private BillingPlan currentPlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "credit_card_id")
	private CreditCard creditCard;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	// 현재 요금제와 멤버 수를 기준으로 월 비용을 계산하는 메소드
	public int calculateMonthlyCost(int memberCount) {
		return currentPlan.getPricePerMember() * memberCount;
	}

	// 현재 요금제에 신용 카드가 필요한지 확인하는 메소드
	public boolean isCreditCardRequired() {
		return currentPlan != BillingPlan.BASIC;
	}
}
