package com.yoyakso.comket.project.event;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;

import lombok.Getter;

/**
 * Event that is published when a member is invited to a project.
 * This allows other services to react to project invitations without direct dependencies.
 */
@Getter
public class ProjectInviteEvent {
	private final Project project;
	private final Member invitedMember;

	public ProjectInviteEvent(Project project, Member invitedMember) {
		this.project = project;
		this.invitedMember = invitedMember;
	}
}