package com.yoyakso.comket.workspace.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.workspace.dto.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.enums.Visibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "workspace")
public class Workspace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Size(min = 2, max = 100)
	@Column(length = 100)
	private String name;

	@Size(min = 0, max = 255)
	@Column(length = 255)
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 10)
	private Visibility visibility;

	@Column(updatable = true)
	private boolean isDeleted;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public static Workspace fromRequest(WorkspaceRegisterRequest workspaceRegisterRequest) {
		return Workspace.builder()
			.name(workspaceRegisterRequest.getName())
			.description(workspaceRegisterRequest.getDescription())
			.visibility(workspaceRegisterRequest.getVisibility())
			.isDeleted(false)
			.build();
	}

	public static Workspace fromRequest(WorkspaceUpdateRequest workspaceUpdateRequest) {
		return Workspace.builder()
			.name(workspaceUpdateRequest.getName())
			.description(workspaceUpdateRequest.getDescription())
			.visibility(workspaceUpdateRequest.getVisibility())
			.isDeleted(false)
			.build();
	}
}
