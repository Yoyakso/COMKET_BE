package com.yoyakso.comket.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findAllByWorkspaceAndIsPublicTrue(Workspace workspace);

	boolean existsByName(@NotNull @Size(min = 2, max = 100) String name);
}
