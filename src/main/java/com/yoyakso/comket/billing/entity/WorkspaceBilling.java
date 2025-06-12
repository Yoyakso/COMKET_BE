package com.yoyakso.comket.billing.entity;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.billing.enums.BillingPlan;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "workspace_billing")
public class WorkspaceBilling {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id", nullable = false)
	private Workspace workspace;

	@NotNull
	@Enumerated(EnumType.STRING)
	private BillingPlan billingPlan;

	@Column(nullable = false)
	private Integer memberCount;

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer month;

	@Column(nullable = false)
	private Integer amount;  // 확정된 금액

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	// 현재 월에 대한 히스토리 기록을 생성하는 헬퍼 메소드
	public static WorkspaceBilling createForCurrentMonth(Workspace workspace, BillingPlan plan, int memberCount,
		int amount) {
		YearMonth currentMonth = YearMonth.now();
		return WorkspaceBilling.builder()
			.workspace(workspace)
			.billingPlan(plan)
			.memberCount(memberCount)
			.year(currentMonth.getYear())
			.month(currentMonth.getMonthValue())
			.amount(amount)
			.build();
	}

	// 특정 월에 대한 히스토리 기록을 생성하는 헬퍼 메소드
	public static WorkspaceBilling createForMonth(Workspace workspace, BillingPlan plan, int memberCount,
		int year, int month, int amount) {
		return WorkspaceBilling.builder()
			.workspace(workspace)
			.billingPlan(plan)
			.memberCount(memberCount)
			.year(year)
			.month(month)
			.amount(amount)
			.build();
	}

	// YearMonth 표현을 가져오는 헬퍼 메소드
	public YearMonth getYearMonth() {
		return year != null && month != null ? YearMonth.of(year, month) : null;
	}
}
