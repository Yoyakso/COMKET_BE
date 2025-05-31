package com.yoyakso.comket.ai.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.yoyakso.comket.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpenAiClient {
	private final RestTemplate restTemplate = new RestTemplate();
	@Value("${spring.ai.openai.api-key}")
	private String apiKey;
	@Value("${spring.ai.openai.api-url}")
	private String apiUrl;

	public String getAiSummary(String prompt) {
		// API 요청용 JSON 파라미터
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(apiKey);

		Map<String, Object> requestBody = Map.of(
			"model", "gpt-4",
			"messages", List.of(
				Map.of("role", "system", "content", "너는 협업 툴의 AI 요약봇이야. 요약을 아래 포맷에 맞춰 반환해."),
				Map.of("role", "user", "content", prompt)
			),
			"temperature", 0.5
		);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
			List<Map<String, Object>> choices = (List<Map<String, Object>>)response.getBody().get("choices");
			return (String)((Map)choices.get(0).get("message")).get("content");
		} catch (Exception e) {
			throw new CustomException("AI_API_ERROR", "AI 요약 호출에 실패했습니다." + e.getMessage());
		}
	}
}
