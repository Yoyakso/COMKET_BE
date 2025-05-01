package com.yoyakso.comket.workspace.dto;

import com.yoyakso.comket.workspace.enums.Visibility;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkspaceRegisterRequest {

	@NotNull(message = "name is required")
	@Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
	private String name;

	@Size(min = 0, max = 255, message = "description must be between 0 and 255 characters")
	private String description;

	@NotNull(message = "visibility is required")
	private Visibility visibility;
}
