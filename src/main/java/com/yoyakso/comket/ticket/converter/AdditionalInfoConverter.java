package com.yoyakso.comket.ticket.converter;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.exception.CustomException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AdditionalInfoConverter implements AttributeConverter<Map<String, Object>, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(Map<String, Object> attribute) {
		try {
			return objectMapper.writeValueAsString(attribute);
		} catch (Exception e) {
			throw new CustomException("ADDITIONALINFO_DATABASE_CONVERT_FAILED",
				"Failed to convert additional info to JSON");
		}
	}

	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) {
		try {
			return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			throw new CustomException("ADDITIONALINFO_ENTITY_CONVERT_FAILED",
				"Failed to convert JSON to additional info map");
		}
	}
}