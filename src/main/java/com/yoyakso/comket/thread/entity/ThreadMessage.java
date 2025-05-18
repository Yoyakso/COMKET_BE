package com.yoyakso.comket.thread.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "thread_message")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long ticketId;

	@Column(nullable = false)
	private Long senderMemberId;

	@Column(nullable = false, length = 255)
	@Size(min = 0, max = 255)
	private String content;

	@Column(nullable = false)
	private LocalDateTime sentAt;
}
