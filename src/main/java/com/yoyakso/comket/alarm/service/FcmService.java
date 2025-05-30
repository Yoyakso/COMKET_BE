package com.yoyakso.comket.alarm.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;

	public FcmService(FirebaseMessaging firebaseMessaging) {
		this.firebaseMessaging = firebaseMessaging;
	}

	public String sendNotification(String title, String body, String token) {
		try {
			Notification notification = Notification.builder()
				.setTitle(title)
				.setBody(body)
				.build();

			Message message = Message.builder()
				.setToken(token)
				.setNotification(notification)
				.build();

			return firebaseMessaging.send(message);
		} catch (Exception e) {
			throw new RuntimeException("FCM 메시지 전송 실패", e);
		}
	}
}
