package com.yoyakso.comket.workspaceMember.service;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceMemberService {

	private final WorkspaceMemberRepository workspaceMemberRepository;

	public void createWorkspaceMember(Workspace workspace, Member member, boolean isActive, String positionType) {
		// 워크스페이스 멤버 생성
		WorkspaceMember workspaceMember = WorkspaceMember.builder()
			.workspace(workspace)
			.member(member)
			.isActive(isActive)
			.positionType(positionType)
			.build();
		// 워크스페이스 멤버 저장
		workspaceMemberRepository.save(workspaceMember);
	}

	public List<Workspace> getWorkspacesByMember(Member member) {
		return workspaceMemberRepository.findByMember(member).stream()
			.map(WorkspaceMember::getWorkspace)
			.filter(
				workspace -> EnumSet.of(WorkspaceState.ACTIVE, WorkspaceState.INACTIVE).contains(workspace.getState()))
			.collect(Collectors.toList());
	}

	public WorkspaceMember getWorkspaceMemberById(Long id) {
		return workspaceMemberRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("WorkspaceMember not found with id: " + id));
	}

	public WorkspaceMember getWorkspaceMemberByWorkspaceIdAndMemberId(Long workspaceId, Long memberId) {
		return workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspaceId, memberId)
			.orElseThrow(() -> new IllegalArgumentException(
				"WorkspaceMember not found with workspaceId: " + workspaceId + " and memberId: " + memberId));
	}

	public List<WorkspaceMember> getAllWorkspaceMembers() {
		return workspaceMemberRepository.findAll();
	}

	public WorkspaceMember updateWorkspaceMember(Long id, WorkspaceMember updatedWorkspaceMember) {
		WorkspaceMember existingWorkspaceMember = workspaceMemberRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("WorkspaceMember not found with id: " + id));
		existingWorkspaceMember.setWorkspace(updatedWorkspaceMember.getWorkspace());
		existingWorkspaceMember.setMember(updatedWorkspaceMember.getMember());
		existingWorkspaceMember.setActive(updatedWorkspaceMember.isActive());
		existingWorkspaceMember.setPositionType(updatedWorkspaceMember.getPositionType());
		return workspaceMemberRepository.save(existingWorkspaceMember);
	}
}