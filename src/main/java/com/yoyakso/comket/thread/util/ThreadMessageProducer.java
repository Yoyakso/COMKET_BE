package com.yoyakso.comket.thread.util;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThreadMessageProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String topic, String message) {
		kafkaTemplate.send(topic, message);
	}
}
