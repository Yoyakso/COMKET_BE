package com.yoyakso.comket.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yoyakso.comket.ai.entity.AiActionItem;

public interface AiActionItemsRepository extends JpaRepository<AiActionItem, Long> {
	List<AiActionItem> findAllByAiSummaryId(Long aiSummaryId);
}
