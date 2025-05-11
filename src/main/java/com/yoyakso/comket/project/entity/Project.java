package com.yoyakso.comket.project.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.file.entity.File;
import com.yoyakso.comket.project.enums.ProjectState;
import com.yoyakso.comket.workspace.entity.Workspace;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

	@Size(min = 0, max = 255)
	@Column(length = 255)
	private String description;

	@ElementCollection // List 사용
	@CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
	@Column(name = "tag")
	private List<String> tags = new ArrayList<>();

	@NotNull
	private Boolean isPublic;

	private ProjectState state;

	@OneToOne
	@JoinColumn(name = "profile_file_id", referencedColumnName = "id", nullable = true)
	private File profileFile;

	@CreationTimestamp
	private LocalDateTime createTime;

	@UpdateTimestamp
	private LocalDateTime updateTime;

	public void updateState(ProjectState state) {
		this.state = state;
	}

	public void updateProjectPublicity(Boolean state) {
		this.isPublic = state;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateProfileFile(File profileFile) {
		this.profileFile = profileFile;
	}

	public void updateTags(List<String> tags) {
		this.tags = tags;
	}
}
