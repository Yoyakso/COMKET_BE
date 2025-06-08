package com.yoyakso.comket.workspace.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.entity.Workspace;

import lombok.Getter;

/**
 * Event that is published when a member is invited to a workspace.
 * This allows other services to react to workspace invitations without direct dependencies.
 */
@Getter
public class WorkspaceInviteEvent {
	private final Workspace workspace;
	private final Member invitedMember;

	public WorkspaceInviteEvent(Workspace workspace, Member invitedMember) {
		this.workspace = workspace;
		this.invitedMember = invitedMember;
	}
}