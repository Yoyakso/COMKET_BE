package com.yoyakso.comket.admin.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorStatisticsResponse {
    
    // 활성 워크스페이스 top 10
    private List<Map<String, Object>> topWorkspaces;
    
    // 티켓 템플릿 사용 비중
    private Map<String, Long> ticketTypeDistribution;
    
    // 티켓 상태 분포
    private Map<String, Long> ticketStateDistribution;
    
    // 스레드 당 평균 메세지 수
    private double avgMessagesPerThread;
    
    // 1인당 평균 담당 티켓 수
    private double avgTicketsPerMember;
}