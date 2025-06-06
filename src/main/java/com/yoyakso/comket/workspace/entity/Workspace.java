package com.yoyakso.comket.workspace.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.workspace.dto.request.WorkspaceRegisterRequest;
import com.yoyakso.comket.workspace.dto.request.WorkspaceUpdateRequest;
import com.yoyakso.comket.workspace.enums.WorkspaceState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
	private Boolean isPublic;

	private WorkspaceState state;

	@NotNull
	private String slug;

	@OneToOne
	@JoinColumn(name = "profile_file_id", referencedColumnName = "id", nullable = true)
	private File profileFile;

	@NotNull
	private String inviteCode;

	@CreationTimestamp
	private LocalDateTime createdAt;

	@UpdateTimestamp
	private LocalDateTime updatedAt;

	public static Workspace fromRequest(WorkspaceRegisterRequest workspaceRegisterRequest) {
		return Workspace.builder()
			.name(workspaceRegisterRequest.getName())
			.description(workspaceRegisterRequest.getDescription())
			.isPublic(workspaceRegisterRequest.getIsPublic())
			.state(WorkspaceState.ACTIVE)
			.slug(workspaceRegisterRequest.getSlug())
			.build();
	}

	public static Workspace fromRequest(WorkspaceUpdateRequest workspaceUpdateRequest) {
		return Workspace.builder()
			.name(workspaceUpdateRequest.getName())
			.description(workspaceUpdateRequest.getDescription())
			.isPublic(workspaceUpdateRequest.getIsPublic())
			.state(workspaceUpdateRequest.getState())
			.slug(workspaceUpdateRequest.getSlug())
			.build();
	}
}
