package com.yoyakso.comket.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.ai.dto.AiEyeLevelSummaryResponse;
import com.yoyakso.comket.ai.dto.AiSummaryWithActionItemsResponse;
import com.yoyakso.comket.ai.enums.SummaryType;
import com.yoyakso.comket.ai.service.AiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class AiController {
	private final AiService aiService;

	@GetMapping("{ticketId}/ai/summary")
	public ResponseEntity<AiSummaryWithActionItemsResponse> getSummaryAndActionItems(
		@PathVariable("ticketId") Long ticketId
	) {
		AiSummaryWithActionItemsResponse response = aiService.getAiSummaryAndActionItems(ticketId);
		return ResponseEntity.ok(response);
	}

	@GetMapping("{ticketId}/ai/eyelevel")
	public ResponseEntity<AiEyeLevelSummaryResponse> getEyeLevelSummary(
		@PathVariable("ticketId") Long ticketId,
		@RequestParam("responsibility") SummaryType responsibility
	) {
		AiEyeLevelSummaryResponse eyeLevelResponse = aiService.getAiEyeLevelSummary(ticketId, responsibility);
		return ResponseEntity.ok(eyeLevelResponse);
	}

	@GetMapping("{ticketId}/ai/history")
	public ResponseEntity<AiSummaryWithActionItemsResponse[]> getHistoryAndActionItems(
		@PathVariable("ticketId") Long ticketId
	) {
		AiSummaryWithActionItemsResponse[] summaryList = aiService.getSummaryHistoryAll(ticketId);
		return ResponseEntity.ok(summaryList);
	}

}
