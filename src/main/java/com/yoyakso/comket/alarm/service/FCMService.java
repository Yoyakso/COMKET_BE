package com.yoyakso.comket.alarm.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.yoyakso.comket.member.entity.Member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {
	private static final int FCM_TOKEN_EXPIRY_DAYS = 30;
	private final FirebaseApp firebaseApp;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * FCM 토큰을 Redis에 저장합니다.
	 *
	 * @param member 사용자 정보
	 * @param token FCM 토큰
	 */
	public void saveFcmToken(Member member, String token) {
		String key = generateFcmTokenKey(member.getId());
		redisTemplate.opsForValue().set(key, token);
		redisTemplate.expire(key, FCM_TOKEN_EXPIRY_DAYS, TimeUnit.DAYS);
		log.info("FCM 토큰 저장 완료: memberId={}", member.getId());
	}

	/**
	 * 사용자의 FCM 토큰을 조회합니다.
	 *
	 * @param memberId 사용자 ID
	 * @return FCM 토큰 (없는 경우 null)
	 */
	public String getFcmToken(Long memberId) {
		String key = generateFcmTokenKey(memberId);
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * 사용자의 FCM 토큰을 삭제합니다.
	 *
	 * @param memberId 사용자 ID
	 */
	public void deleteFcmToken(Long memberId) {
		String key = generateFcmTokenKey(memberId);
		redisTemplate.delete(key);
		log.info("FCM 토큰 삭제 완료: memberId={}", memberId);
	}

	/**
	 * 단일 사용자에게 FCM 알림을 전송합니다.
	 */
	public void sendNotification(String token, String title, String body, Map<String, String> data) {
		try {
			Message message = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
					.setTitle(title)
					.setBody(body)
					.build())
				.putAllData(data)
				.build();

			String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
			log.info("FCM 알림 전송 성공: {}", response);
		} catch (FirebaseMessagingException e) {
			log.error("FCM 알림 전송 실패", e);
		}
	}

	/**
	 * 여러 사용자에게 FCM 알림을 전송합니다.
	 */
	public void sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) {
		try {
			MulticastMessage message = MulticastMessage.builder()
				.addAllTokens(tokens)
				.setNotification(Notification.builder()
					.setTitle(title)
					.setBody(body)
					.build())
				.putAllData(data)
				.build();

			BatchResponse response = FirebaseMessaging.getInstance(firebaseApp).sendMulticast(message);
			log.info("FCM 멀티캐스트 알림 전송 성공: {}/{}", response.getSuccessCount(), tokens.size());
		} catch (FirebaseMessagingException e) {
			log.error("FCM 멀티캐스트 알림 전송 실패", e);
		}
	}

	/**
	 * FCM 토큰 저장을 위한 Redis 키를 생성합니다.
	 *
	 * @param memberId 사용자 ID
	 * @return Redis 키
	 */
	private String generateFcmTokenKey(Long memberId) {
		return "fcm:token:memberId:" + memberId;
	}
}