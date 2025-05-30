package com.yoyakso.comket.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoyakso.comket.notification.service.FcmService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

	private final FcmService fcmService;

	public NotificationController(FcmService fcmService) {
		this.fcmService = fcmService;
	}

	@PostMapping("/send")
	public String sendNotification(
		@RequestParam String title,
		@RequestParam String body,
		@RequestParam String token
	) {
		return fcmService.sendNotification(title, body, token);
	}
}