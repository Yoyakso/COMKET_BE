package com.yoyakso.comket.thread.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
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
	private Long senderWorkspaceMemberId;

	@Nullable
	@Column(nullable = true)
	private Long parentThreadId;

	@Column(nullable = false)
	@Lob
	private String content;

	@Column(nullable = false)
	private LocalDateTime sentAt;

	@Column(nullable = false)
	private Boolean isModified;

	@Lob
	@Nullable
	@Column(nullable = true)
	private String resources;

	@OneToMany(mappedBy = "threadMessage", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<ThreadMessageMention> mentions = new ArrayList<>();

	public void setMentions(List<ThreadMessageMention> mentions) {
		this.mentions = mentions;
	}

	public void editContent(String content) {
		this.content = content;
	}

	public void setIsModified() {
		this.isModified = true;
	}

	public void editResources(String resourcesJson) {
		this.resources = resourcesJson;
	}
}
