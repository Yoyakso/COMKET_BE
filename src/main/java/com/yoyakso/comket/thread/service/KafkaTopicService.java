package com.yoyakso.comket.thread.service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaTopicService {

	private final KafkaAdmin kafkaAdmin;

	public void createThreadTopicIfNotExists(Long ticketId) {
		String topicName = "thread-ticket-" + ticketId;

		NewTopic topic = TopicBuilder.name(topicName)
			.partitions(1)
			.replicas(1)
			.build();

		try {
			kafkaAdmin.createOrModifyTopics(topic);
		} catch (Exception e) {
			// TODO: 로그 추가, Exception을 발생시키지는 않음.
			// Kafka의 동작은 Transaction으로 묶이지 않음. 따라서 티켓 생성 시 스레드를 생성해보고, 만약 실패하면 첫 메시지 전송 시 재확인
			System.out.println("@@@ 스레드 생성 실패" + e.getMessage());
		}
	}
}
