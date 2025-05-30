package com.yoyakso.comket.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.ai.entity.AiSummary;

public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {
	List<AiSummary> findAllByTicketIdOrderByCreateTimeAsc(Long ticketId);
}
