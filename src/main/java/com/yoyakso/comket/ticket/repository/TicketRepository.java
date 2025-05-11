package com.yoyakso.comket.ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	List<Ticket> findByProject(Project project);
}