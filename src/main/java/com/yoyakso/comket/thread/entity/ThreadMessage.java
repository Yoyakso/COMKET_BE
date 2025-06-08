package com.yoyakso.comket.thread.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
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

	private Long parentThreadId;

	@Column(nullable = false)
	@Lob
	private String content;

	@Column(nullable = false)
	private LocalDateTime sentAt;

	@Column(nullable = false)
	private Boolean isModified;

	public void editContent(String content) {
		this.content = content;
	}
}
