package com.yoyakso.comket.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

	@Value("${firebase.credentials.path}")
	private String firebaseCredentialsPath;

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {
			// 먼저 파일 시스템에서 찾고, 없으면 클래스패스에서 찾기
			Resource resource;
			File file = new File(firebaseCredentialsPath);
			if (file.exists()) {
				resource = new FileSystemResource(file);
			} else {
				resource = new ClassPathResource(firebaseCredentialsPath);
			}

			InputStream serviceAccount = resource.getInputStream();
			FirebaseOptions options = FirebaseOptions.builder()
				.setCredentials(GoogleCredentials.fromStream(serviceAccount))
				.build();
			return FirebaseApp.initializeApp(options);
		} else {
			return FirebaseApp.getInstance();
		}
	}
}
