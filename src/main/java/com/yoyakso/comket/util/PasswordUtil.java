package com.yoyakso.comket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
	private static final PasswordEncoder passwordEndcoder= new BCryptPasswordEncoder();

	public static String encodePassword(String password) {
		return passwordEndcoder.encode(password);
	}

	public static boolean matches(String rawPassword, String encodedPassword) {
		return passwordEndcoder.matches(rawPassword, encodedPassword);
	}
}
