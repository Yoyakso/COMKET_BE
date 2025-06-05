package com.yoyakso.comket.workspaceMember.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.dto.WorkspaceMemberCreateRequest;
import com.yoyakso.comket.workspace.dto.WorkspaceMemberInfoResponse;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceMemberService {

	private final WorkspaceMemberRepository workspaceMemberRepository;

	private final MemberService memberService;

	private final EmailService emailService;

	public void createWorkspaceMember(Workspace workspace, Member member, WorkspaceMemberState state,
		String positionType) {
		// 워크스페이스 멤버 생성
		WorkspaceMember workspaceMember = WorkspaceMember.builder()
			.workspace(workspace)
			.member(member)
			.state(state)
			.positionType(positionType)
			.build();
		// 워크스페이스 멤버 저장
		workspaceMemberRepository.save(workspaceMember);
	}

	public List<Workspace> getWorkspacesByMember(Member member) {
		return workspaceMemberRepository.findByMember(member).stream()
			.filter(
				workspaceMember -> workspaceMember.getState() == WorkspaceMemberState.ACTIVE) // WorkspaceMember 상태 필터링
			.map(WorkspaceMember::getWorkspace)
			.filter(workspace -> EnumSet.of(WorkspaceState.ACTIVE, WorkspaceState.INACTIVE)
				.contains(workspace.getState())) // Workspace 상태 필터링
			.collect(Collectors.toList());
	}

	public WorkspaceMember getWorkspaceMemberById(Long id) {
		return workspaceMemberRepository.findById(id)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_WORKSPACEMEMBER", "워크스페이스 멤버를 찾을 수 없습니다."));
	}

	public WorkspaceMember getWorkspaceMemberByWorkspaceIdAndMemberId(Long workspaceId, Long memberId) {
		return workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspaceId, memberId)
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_WORKSPACEMEMBER", "워크스페이스 멤버를 찾을 수 없습니다."));
	}

	public List<WorkspaceMember> getAllWorkspaceMembers() {
		return workspaceMemberRepository.findAll();
	}

	public WorkspaceMember updateWorkspaceMember(WorkspaceMember updatedWorkspaceMember) {
		WorkspaceMember existingWorkspaceMember = workspaceMemberRepository.findById(updatedWorkspaceMember.getId())
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_WORKSPACEMEMBER", "워크스페이스 멤버를 찾을 수 없습니다."));
		existingWorkspaceMember.setWorkspace(updatedWorkspaceMember.getWorkspace());
		existingWorkspaceMember.setMember(updatedWorkspaceMember.getMember());
		existingWorkspaceMember.setState(updatedWorkspaceMember.getState());
		existingWorkspaceMember.setPositionType(updatedWorkspaceMember.getPositionType());
		return workspaceMemberRepository.save(existingWorkspaceMember);
	}

	public List<WorkspaceMember> searchWorkspaceMembers(Long workspaceId, String keyword, List<String> positionTypes,
		List<String> memberStates) {
		return workspaceMemberRepository.searchWorkspaceMembers(workspaceId, keyword, positionTypes, memberStates);
	}

	public List<WorkspaceMember> getWorkspaceMembersByWorkspaceId(Long id) {
		return workspaceMemberRepository.findByWorkspaceId(id);
	}

	public List<WorkspaceMemberInfoResponse> inviteMembersToWorkspace(Workspace workspace,
		WorkspaceMemberCreateRequest workspaceMemberCreateRequest) {
		List<String> memberEmailList = workspaceMemberCreateRequest.getMemberEmailList();
		List<String> newMemberEmailList = filterNewMemberEmails(workspace.getId(), memberEmailList);
		createWorkspaceMembers(workspace, newMemberEmailList, workspaceMemberCreateRequest);
		return buildResponse(workspace.getId(), newMemberEmailList);
	}

	private List<Long> validateMemberIdList(List<Long> memberIdList) {
		if (memberIdList == null || memberIdList.isEmpty()) {
			throw new CustomException("INVALID_MEMBER_LIST", "초대할 멤버 ID 리스트가 비어 있습니다.");
		}
		return memberIdList;
	}

	private List<String> filterNewMemberEmails(Long workspaceId, List<String> memberEmailList) {
		List<String> existingMemberEmails = getWorkspaceMembersByWorkspaceId(workspaceId).stream()
			.filter(workspaceMember -> workspaceMember.getState() != WorkspaceMemberState.DELETED)
			.map(workspaceMember -> workspaceMember.getMember().getEmail()) // 이메일 추출
			.toList();

		List<String> newMemberEmails = memberEmailList.stream()
			.filter(memberEmail -> !existingMemberEmails.contains(memberEmail)) // 이메일 비교
			.toList();

		if (newMemberEmails.isEmpty()) {
			throw new CustomException("MEMBER_ALREADY_INVITED", "이미 초대된 멤버가 있습니다.");
		}
		return newMemberEmails;
	}

	private void createWorkspaceMembers(Workspace workspace, List<String> newMemberEmailList,
		WorkspaceMemberCreateRequest workspaceMemberCreateRequest) {
		for (String memberEmail : newMemberEmailList) {
			Optional<Member> memberOptional = memberService.getMemberByEmailOptional(memberEmail);
			// 회원가입되지 않은 멤버 처리
			if (memberOptional.isEmpty()) {
				emailService.sendInvitationEmail(workspace, memberEmail);
				continue;
			}
			Member member = memberOptional.orElseThrow(() ->
				new CustomException("MEMBER_NOT_FOUND", "멤버를 찾을 수 없습니다.")
			);
			WorkspaceMember workspaceMember = WorkspaceMember.builder()
				.workspace(workspace)
				.member(member)
				.state(workspaceMemberCreateRequest.getState())
				.positionType(workspaceMemberCreateRequest.getPositionType())
				.build();
			workspaceMemberRepository.save(workspaceMember);
		}
	}

	private List<WorkspaceMemberInfoResponse> buildResponse(Long workspaceId, List<String> newMemberEmailList) {
		return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
			.filter(workspaceMember -> newMemberEmailList.contains(workspaceMember.getMember().getEmail()))
			.filter(workspaceMember -> workspaceMember.getState() == WorkspaceMemberState.ACTIVE)
			.map(workspaceMember -> WorkspaceMemberInfoResponse.builder()
				.workspaceMemberid(workspaceMember.getId())
				.name(workspaceMember.getMember().getFullName())
				.email(workspaceMember.getMember().getEmail())
				.positionType(workspaceMember.getPositionType())
				.state(workspaceMember.getState())
				.build())
			.toList();
	}
}