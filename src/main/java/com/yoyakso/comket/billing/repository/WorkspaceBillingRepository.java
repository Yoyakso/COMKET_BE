package com.yoyakso.comket.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.billing.entity.WorkspaceBilling;
import com.yoyakso.comket.workspace.entity.Workspace;

@Repository
public interface WorkspaceBillingRepository extends JpaRepository<WorkspaceBilling, Long> {
    Optional<WorkspaceBilling> findByWorkspace(Workspace workspace);
    Optional<WorkspaceBilling> findByWorkspaceId(Long workspaceId);
}