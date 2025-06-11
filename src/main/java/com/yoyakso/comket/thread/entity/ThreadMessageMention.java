package com.yoyakso.comket.thread.entity;

import com.yoyakso.comket.projectMember.entity.ProjectMember;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thread_message_mention")
public class ThreadMessageMention {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "thread_message_id", nullable = false)
	private ThreadMessage threadMessage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_member_id", nullable = false)
	private ProjectMember mentionedMember;
}
