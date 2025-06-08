package com.yoyakso.comket.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.billing.service.WorkspaceMemberHistoryService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/workspace-member-history")
@RequiredArgsConstructor
public class WorkspaceMemberHistoryController {

    private final WorkspaceMemberHistoryService workspaceMemberHistoryService;
    private final MemberService memberService;

    @PostMapping("/record")
    @Operation(summary = "워크스페이스 멤버 히스토리 수동 기록 API", description = "워크스페이스 멤버 수를 수동으로 기록하는 API")
    public ResponseEntity<Void> recordWorkspaceMemberHistory() {
        // 인증된 사용자 확인
        memberService.getAuthenticatedMember();

        // 워크스페이스 멤버 히스토리 기록
        workspaceMemberHistoryService.recordMonthlyWorkspaceMemberCounts();

        return ResponseEntity.ok().build();
    }
}
