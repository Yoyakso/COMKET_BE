package com.yoyakso.comket.workspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.workspace.enums.WorkspaceState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
