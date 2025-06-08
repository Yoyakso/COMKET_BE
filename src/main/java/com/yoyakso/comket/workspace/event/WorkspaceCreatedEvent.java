package com.yoyakso.comket.workspace.event;

import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.Getter;

/**
 * Event that is published when a new workspace is created.
 * This allows other services to react to workspace creation without direct dependencies.
 */
@Getter
public class WorkspaceCreatedEvent {
	private final Workspace workspace;

	public WorkspaceCreatedEvent(Workspace workspace) {
		this.workspace = workspace;
	}
}