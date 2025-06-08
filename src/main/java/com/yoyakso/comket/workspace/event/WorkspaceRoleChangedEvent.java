package com.yoyakso.comket.workspace.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.Getter;

/**
 * Event that is published when a member's role in a workspace is changed.
 * This allows other services to react to role changes without direct dependencies.
 */
@Getter
public class WorkspaceRoleChangedEvent {
	private final Workspace workspace;
	private final Member member;
	private final String oldRole;
	private final String newRole;

	public WorkspaceRoleChangedEvent(Workspace workspace, Member member, String oldRole, String newRole) {
		this.workspace = workspace;
		this.member = member;
		this.oldRole = oldRole;
		this.newRole = newRole;
	}
}