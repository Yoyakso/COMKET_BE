package com.yoyakso.comket.configuration;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.yoyakso.comket.exception.CustomException;

@Configuration
public class FcmConfig {

	@Value("${fcm.certification}")
	private String googleApplicationCredentials;

	@Bean
	FirebaseMessaging firebaseMessaging() {
		try {
			InputStream serviceAccount = new ClassPathResource(googleApplicationCredentials).getInputStream();
			GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount);
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(googleCredentials)
				.build();
			FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
			return FirebaseMessaging.getInstance(firebaseApp);
		} catch (Exception e) {
			throw new CustomException("FCM_INITIALIZATION_FAILED", "FCM 연동 실패");
		}
	}

}
