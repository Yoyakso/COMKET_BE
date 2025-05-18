package com.yoyakso.comket.workspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.workspace.enums.WorkspaceState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceUpdateRequest {
	private String name;
	private String description;
	@JsonProperty("is_public")
	private Boolean isPublic;
	private WorkspaceState state;
	@JsonProperty("profile_file_id")
	private Long profileFileId;
	private String slug;
}
