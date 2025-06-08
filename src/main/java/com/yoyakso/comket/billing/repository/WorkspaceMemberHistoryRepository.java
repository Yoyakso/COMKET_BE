package com.yoyakso.comket.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.billing.entity.WorkspaceMemberHistory;

@Repository
public interface WorkspaceMemberHistoryRepository extends JpaRepository<WorkspaceMemberHistory, Long> {

    /**
     * 특정 워크스페이스의 히스토리 기록 조회
     */
    List<WorkspaceMemberHistory> findByWorkspaceId(Long workspaceId);

    /**
     * 특정 워크스페이스의 히스토리 기록을 년도와 월 기준으로 내림차순 정렬하여 조회
     */
    @Query("SELECT wmh FROM WorkspaceMemberHistory wmh WHERE wmh.workspace.id = :workspaceId ORDER BY wmh.year DESC, wmh.month DESC")
    List<WorkspaceMemberHistory> findByWorkspaceIdOrderByYearMonthDesc(@Param("workspaceId") Long workspaceId);

    /**
     * 특정 워크스페이스의 특정 월 히스토리 기록 조회
     */
    Optional<WorkspaceMemberHistory> findByWorkspaceIdAndYearAndMonth(Long workspaceId, Integer year, Integer month);

    /**
     * 특정 년도와 월에 대한 모든 워크스페이스의 히스토리 기록 조회
     */
    List<WorkspaceMemberHistory> findByYearAndMonth(Integer year, Integer month);
}
