package com.yoyakso.comket.billing.entity;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "workspace_member_history")
public class WorkspaceMemberHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @NotNull
    @Column(nullable = false)
    private Integer month;

    @NotNull
    @Column(nullable = false)
    private Integer memberCount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // 현재 월에 대한 히스토리 기록을 생성하는 헬퍼 메소드
    public static WorkspaceMemberHistory createForCurrentMonth(Workspace workspace, int memberCount) {
        YearMonth currentMonth = YearMonth.now();
        return WorkspaceMemberHistory.builder()
            .workspace(workspace)
            .year(currentMonth.getYear())
            .month(currentMonth.getMonthValue())
            .memberCount(memberCount)
            .build();
    }

    // YearMonth 표현을 가져오는 헬퍼 메소드
    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }
}
