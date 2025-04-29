package com.yoyakso.comket.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.workspace.entity.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
	// Custom query methods can be defined here if needed
	// For example:
	// List<Workspace> findByName(String name);
	// List<Workspace> findByOwnerId(Long ownerId);

}
