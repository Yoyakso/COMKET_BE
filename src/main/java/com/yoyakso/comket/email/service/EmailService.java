package com.yoyakso.comket.email.service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailService {

	private final ConcurrentHashMap<String, Long> verificationTimestamps = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();
	private final Random random = new Random();

	private final JavaMailSender emailSender;
	private final SpringTemplateEngine templateEngine;
	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;

	// 인증 번호 생성 및 저장
	public void createVerificationCode(String email) {
		String code = String.format("%06d", random.nextInt(1000000)); // 6자리 랜덤 숫자 생성
		verificationCodes.put(email, code);
		verificationTimestamps.put(email, System.currentTimeMillis()); // 생성 시간 저장
		sendEmail(email, setVerifyCodeContent(code)); // 이메일 전송
	}

	// 인증 번호 검증
	public void verifyVerificationCode(String email, String code) {
		// 이메일이 존재하는지 검증
		if (!verificationCodes.containsKey(email)) {
			throw new IllegalStateException("해당 이메일의 인증 요청이 존재하지 않습니다.");
		}
		// 인증 번호가 만료되었는지 검증
		long issuedTime = verificationTimestamps.getOrDefault(email, 0L);
		if (System.currentTimeMillis() - issuedTime > authCodeExpirationMillis) {
			verificationCodes.remove(email);
			verificationTimestamps.remove(email);
			throw new IllegalStateException("인증 번호가 만료되었습니다.");
		}

		// 인증 번호가 일치하는지 검증
		String storedCode = verificationCodes.get(email);
		if (storedCode == null) {
			throw new IllegalStateException("인증 번호가 존재하지 않습니다.");
		}
		if (!storedCode.equals(code)) {
			throw new IllegalStateException("인증 번호가 일치하지 않습니다.");
		}
		verificationCodes.remove(email);
		verificationTimestamps.remove(email);
	}

	// 이메일 전송
	private void sendEmail(String email, String context) {
		try {
			// 이메일 설정
			MimeMessage message = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			// HTML 형식으로 이메일 내용 설정
			helper.setTo(email);
			helper.setSubject("이메일 인증 코드");
			helper.setText(context, true);

			// 이메일 전송
			emailSender.send(message);
		} catch (MessagingException e) {
			throw new IllegalStateException("이메일 전송에 실패했습니다.", e);
		}
	}

	public String setVerifyCodeContent(String code) {
		Context context = new Context();
		context.setVariable("code", code);
		context.setVariable("expirationTime", authCodeExpirationMillis / 60000); // 분 단위로 변환

		return templateEngine.process("EmailVerifyFormat", context);
	}
}