package com.yoyakso.comket.inquiry.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.inquiry.enums.InquiryType;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInquiryCreateRequest {

	@NotBlank(message = "이름은 필수 입력 항목입니다.")
	private String name;

	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "유효한 이메일 형식이 아닙니다.")
	private String email;

	@NotNull(message = "문의 유형은 필수 입력 항목입니다.")
	@JsonProperty("inquiry_type")
	private InquiryType type;

	@NotBlank(message = "문의 내용은 필수 입력 항목입니다.")
	@Lob
	private String message;

	@JsonCreator
	public void setType(@JsonProperty("inquiry_type") String type) {
		this.type = (type != null) ? InquiryType.valueOf(type.toUpperCase()) : null;
	}
}