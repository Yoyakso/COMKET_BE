package com.yoyakso.comket.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.billing.entity.WorkspaceBilling;
import com.yoyakso.comket.workspace.entity.Workspace;

@Repository
public interface WorkspaceBillingRepository extends JpaRepository<WorkspaceBilling, Long> {
    Optional<WorkspaceBilling> findByWorkspace(Workspace workspace);

    // 현재 빌링 정보 조회 (히스토리가 아닌 현재 상태)
    @Query("SELECT wb FROM WorkspaceBilling wb WHERE wb.workspace.id = :workspaceId AND (wb.year IS NULL OR wb.month IS NULL)")
    Optional<WorkspaceBilling> findByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 히스토리 조회를 위한 메소드 추가
    @Query("SELECT wb FROM WorkspaceBilling wb WHERE wb.workspace.id = :workspaceId AND wb.year IS NOT NULL AND wb.month IS NOT NULL ORDER BY wb.year DESC, wb.month DESC")
    List<WorkspaceBilling> findHistoryByWorkspaceIdOrderByYearMonthDesc(@Param("workspaceId") Long workspaceId);

    // 현재 빌링 정보 조회 (히스토리가 아닌 현재 상태)
    @Query("SELECT wb FROM WorkspaceBilling wb WHERE wb.workspace.id = :workspaceId AND (wb.year IS NULL OR wb.month IS NULL)")
    Optional<WorkspaceBilling> findCurrentByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 특정 월의 빌링 정보 조회
    Optional<WorkspaceBilling> findByWorkspaceIdAndYearAndMonth(Long workspaceId, Integer year, Integer month);
}
