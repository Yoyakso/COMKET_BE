package com.yoyakso.comket.thread.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

import com.yoyakso.comket.thread.service.KafkaConsumerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThreadKafkaListenerManager {

	// Kafka의 Listner는 빈 등록 시 고정됨. 따라서 여러 채팅방이 있는 우리 구조에서는 동적으로 할당하기 위해 Container를 직접 생성해야함.
	private final ConcurrentKafkaListenerContainerFactory<String, String> listenerFactory;
	private final KafkaConsumerService kafkaConsumerService;
	private final Map<Long, ConcurrentMessageListenerContainer<String, String>> listenerContainers = new ConcurrentHashMap<>();

	public void startListening(Long ticketId) {
		String topic = "thread-ticket-" + ticketId;

		if (listenerContainers.containsKey(ticketId)) {
			return; // 이미 존재하는 경우 리턴
		}

		ContainerProperties containerProps = new ContainerProperties(topic);
		containerProps.setMessageListener((MessageListener<String, String>)record -> {
			System.out.println("[test] - listner");
			kafkaConsumerService.handleMessage(record.value(), ticketId);
		});

		ConcurrentMessageListenerContainer<String, String> container =
			new ConcurrentMessageListenerContainer<>(listenerFactory.getConsumerFactory(), containerProps);

		container.setBeanName("kafkaListener-ticket-" + ticketId);
		container.start();
		listenerContainers.put(ticketId, container);
	}

	public void stopListening(Long ticketId) {
		ConcurrentMessageListenerContainer<String, String> container = listenerContainers.remove(ticketId);
		if (container != null) {
			container.stop();
		}
	}
}