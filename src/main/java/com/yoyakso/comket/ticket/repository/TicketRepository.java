package com.yoyakso.comket.ticket.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findByProjectAndIsDeletedFalse(Project project);

	@Query("SELECT t FROM Ticket t " +
		"JOIN t.project p " +
		"WHERE p.name = :projectName " +
		"AND t.isDeleted = false " +
		"AND (:states IS NULL OR t.state IN :states) " +
		"AND (:priorities IS NULL OR t.priority IN :priorities) " +
		"AND (:assignees IS NULL OR t.assignee.id IN :assignees) " +
		"AND (:endDate IS NULL OR t.endDate <= :endDate) " +
		"AND (:keyword IS NULL OR t.name LIKE %:keyword%)"
	)
	List<Ticket> searchAndFilterTickets(
		@Param("projectName") String projectName,
		@Param("states") List<String> states,
		@Param("priorities") List<String> priorities,
		@Param("assignees") List<Long> assignees,
		@Param("endDate") LocalDate endDate,
		@Param("keyword") String keyword
	);
}