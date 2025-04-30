package com.yoyakso.comket.project.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "project")
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY) // DB 성능 최적화
	@JoinColumn(name = "workspace_id", nullable = false)
	private Workspace workspace; // Workspace 객체로 선언 추후 병합

	@NotNull
	@Size(min = 2, max = 100)
	@Column(length = 100)
	private String name;

	@Column(length = 255)
	private String purpose;

	@Column(length = 255)
	private String description;

	private ProjectState state;

	private Boolean isPublic;

	@CreationTimestamp
	private LocalDateTime createTime;

	@UpdateTimestamp
	private LocalDateTime updateTime;

	@Builder
	public Project(
		Long id,
		Workspace workspace,
		String name,
		String purpose,
		String description,
		ProjectState state,
		Boolean isPublic
	) {
		this.id = id;
		this.workspace = workspace;
		this.name = name;
		this.purpose = purpose;
		this.description = description;
		this.state = state;
		this.isPublic = isPublic;
	}

	public void updateState(ProjectState state) {
		this.state = state;
	}

	public void updateProjectPublicity(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updatePurpose(String purpose) {
		this.purpose = purpose;
	}

	public void updateDescription(String description) {
		this.description = description;
	}
}
