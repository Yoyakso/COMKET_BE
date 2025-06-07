package com.yoyakso.comket.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

	List<Project> findAllByWorkspaceAndIsPublicTrueAndState(Workspace workspace, ProjectState state);

	List<Project> findAllByWorkspaceAndState(Workspace workspace, ProjectState state);

	boolean existsByName(@NotNull @Size(min = 2, max = 100) String name);

	boolean existsByNameAndStateAndWorkspace(@NotNull @Size(min = 2, max = 100) String name, ProjectState state,
		Workspace workspace);

	Optional<Project> findByName(String projectName);

	@Query("""
		SELECT p FROM Project p
		WHERE p.workspace.id = :workspaceId
		  AND p.state = :state
		  AND (
		        p.isPublic = true
		     OR p.id IN (
		         SELECT pm.project.id FROM ProjectMember pm
		         WHERE pm.member.id = :memberId
		     )
		  )
		""")
	List<Project> findProjectsByWorkspaceAndMemberAndState(
		@Param("workspaceId") Long workspaceId,
		@Param("memberId") Long memberId,
		@Param("state") ProjectState state
	);

}
