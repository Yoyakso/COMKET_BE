package com.yoyakso.comket.alarm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.alarm.entity.Alarm;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
	Optional<Alarm> findByMemberIdAndProjectId(Long memberId, Long projectId);

	@Query(
		"SELECT a " +
			"FROM Alarm a JOIN Project p ON a.project.id = p.id " +
			"WHERE a.member.id = :memberId AND p.workspace.id = :workspaceId"
	)
	List<Alarm> findByMemberIdAndWorkspaceId(@Param("memberId") Long memberId,
		@Param("workspaceId") Long workspaceId);
}
