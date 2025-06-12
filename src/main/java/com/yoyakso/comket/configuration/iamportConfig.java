package com.yoyakso.comket.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siot.IamportRestClient.IamportClient;

@Configuration
public class iamportConfig {

	@Value("${iamport.api.key}")
	private String apiKey;

	@Value("${iamport.api.secret}")
	private String secret;

	@Bean
	public IamportClient iamportClient() {
		return new IamportClient(apiKey, secret);
	}
}
