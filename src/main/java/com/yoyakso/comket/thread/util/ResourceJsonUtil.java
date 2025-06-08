package com.yoyakso.comket.thread.util;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResourceJsonUtil {

	private final ObjectMapper objectMapper;

	// List<String> → JSON String 변환
	public String toJson(List<String> resources) {
		if (resources == null || resources.isEmpty()) {
			return null;
		}

		try {
			return objectMapper.writeValueAsString(resources);
		} catch (JsonProcessingException e) {
			throw new CustomException("THREAD_RESOURCE_UPLOAD_FAIL", "스레드 리소스 업로드에 실패했습니다." + e.getMessage());
		}
	}

	public List<String> fromJson(String resourcesJson) {
		if (resourcesJson == null || resourcesJson.isEmpty()) {
			return Collections.emptyList();
		}

		try {
			return objectMapper.readValue(resourcesJson, new TypeReference<List<String>>() {
			});
		} catch (Exception e) {
			throw new CustomException("THREAD_RESOURCE_DOWNLOAD_FAIL", "스레드 리소스 다운로드에 실패했습니다." + e.getMessage());
		}
	}
}