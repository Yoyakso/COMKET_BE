package com.yoyakso.comket.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.billing.entity.WorkspacePlan;
import com.yoyakso.comket.workspace.entity.Workspace;

@Repository
public interface WorkspacePlanRepository extends JpaRepository<WorkspacePlan, Long> {
    Optional<WorkspacePlan> findByWorkspace(Workspace workspace);
    Optional<WorkspacePlan> findByWorkspaceId(Long workspaceId);
}