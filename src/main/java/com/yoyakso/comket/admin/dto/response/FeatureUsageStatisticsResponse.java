package com.yoyakso.comket.admin.dto.response;

import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureUsageStatisticsResponse {

    // AI 요약 기능 사용 횟수
    private long aiSummaryCount;

    // AI 요약 기능 일별 사용 횟수
    private Map<LocalDate, Long> aiSummaryDailyUsage;

    // AI 요약 직군별 사용 횟수
    private Map<String, Long> aiSummaryTypeDistribution;

    // 파일 업로드 횟수
    private long fileCount;

    // 총 파일 용량
    private long totalFileSize;
}
