package com.yoyakso.comket.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.ticket.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}