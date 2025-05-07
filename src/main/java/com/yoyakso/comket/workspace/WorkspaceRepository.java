package com.yoyakso.comket.workspace;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
	boolean existsByName(@NotNull @Size(min = 2, max = 100) String name);

	Optional<Workspace> findByName(String name);

	boolean existsByInviteCode(String inviteCode);

	boolean existsBySlug(String slug);

	Optional<Workspace> findBySlug(String slug);

	Optional<Workspace> findByInviteCode(String inviteCode);
}
