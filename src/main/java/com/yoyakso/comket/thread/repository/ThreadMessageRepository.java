package com.yoyakso.comket.thread.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.thread.entity.ThreadMessage;

public interface ThreadMessageRepository extends JpaRepository<ThreadMessage, Long> {
	List<ThreadMessage> findAllByTicketIdOrderBySentAtAsc(Long ticketId);
}
