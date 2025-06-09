package com.yoyakso.comket.alarm.controller;

import com.yoyakso.comket.alarm.dto.FCMTestRequest;
import com.yoyakso.comket.alarm.dto.FCMTokenRequest;
import com.yoyakso.comket.alarm.service.FCMService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM", description = "FCM 관련 API")
public class FCMController {
    private final FCMService fcmService;
    private final MemberService memberService;

    @PostMapping("/register")
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    public ResponseEntity<Void> registerFcmToken(@RequestBody FCMTokenRequest request) {
        Member member = memberService.getAuthenticatedMember();
        fcmService.saveFcmToken(member, request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    @Operation(summary = "FCM 테스트", description = "현재 로그인한 사용자에게 테스트 알림을 전송합니다.")
    public ResponseEntity<Void> testFcmNotification(@RequestBody FCMTestRequest request) {
        Member member = memberService.getAuthenticatedMember();
        String token = fcmService.getFcmToken(member.getId());

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        fcmService.sendNotification(token, request.getTitle(), request.getBody(), request.getData());
        return ResponseEntity.ok().build();
    }
}
