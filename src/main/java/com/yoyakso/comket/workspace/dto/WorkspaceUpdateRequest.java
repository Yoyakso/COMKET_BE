package com.yoyakso.comket.workspace.dto;

import com.yoyakso.comket.workspace.enums.Visibility;

import lombok.Data;

@Data
public class WorkspaceUpdateRequest {
	private String name;
	private String description;
	// private String imageUrl;
	private Visibility visibility; // PUBLIC, PRIVATE
}
