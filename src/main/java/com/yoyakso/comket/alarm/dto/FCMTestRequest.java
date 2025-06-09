package com.yoyakso.comket.alarm.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMTestRequest {
    private String title;
    private String body;
    
    @Builder.Default
    private Map<String, String> data = new HashMap<>();
}