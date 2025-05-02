package com.yoyakso.comket.workspace;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.Visibility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
	boolean existsByName(@NotNull @Size(min = 2, max = 100) String name);

	boolean existsByVisibility(Visibility visibility);
}
