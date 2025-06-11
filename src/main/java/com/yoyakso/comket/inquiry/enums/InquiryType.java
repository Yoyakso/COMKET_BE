package com.yoyakso.comket.inquiry.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum InquiryType {

    // 문의 유형: 제품 문의, 요금제 문의, 기술 지원, 파트너십, 기타
    PRODUCT("제품 문의"),
    PRICING("요금제 문의"),
    TECHNICAL("기술 지원"),
    PARTNERSHIP("파트너십"),
    OTHER("기타");

    @JsonValue
    private final String type;

    InquiryType(String type) {
        this.type = type;
    }
}