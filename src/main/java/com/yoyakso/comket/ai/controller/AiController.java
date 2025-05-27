package com.yoyakso.comket.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.ai.dto.AiEyeLevelSummaryResponse;
import com.yoyakso.comket.ai.dto.AiSummaryWithActionItemsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class AiController {

	@GetMapping("{ticketId}/ai")
	public ResponseEntity<AiSummaryWithActionItemsResponse> getSummaryAndActionItems(
		@PathVariable("ticketId") Long ticketId
	) {
		
	}

	@GetMapping("{ticketId}/ai")
	public ResponseEntity<AiEyeLevelSummaryResponse> getEyeLevelSummary(
		@PathVariable("ticketId") Long ticketId,
		@RequestParam("responsibility") String responsibility
	) {

	}

}
