package com.yoyakso.comket.workspace.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceRegisterRequest {

	@NotNull(message = "name is required")
	@Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
	private String name;

	@Size(min = 0, max = 255, message = "description must be between 0 and 255 characters")
	private String description;

	@NotNull(message = "isPublic is required")
	@JsonProperty("is_public")
	private Boolean isPublic;

	@JsonProperty("profile_file_id")
	private Long profileFileId;

	@NotNull(message = "slug is required")
	private String slug;
}
