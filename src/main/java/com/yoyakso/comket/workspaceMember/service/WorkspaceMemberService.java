package com.yoyakso.comket.workspaceMember.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.file.service.FileService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.workspace.dto.request.WorkspaceMemberCreateRequest;
import com.yoyakso.comket.workspace.dto.response.WorkspaceMemberInfoResponse;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
import com.yoyakso.comket.workspace.event.WorkspaceInviteEvent;
import com.yoyakso.comket.workspace.event.WorkspaceRoleChangedEvent;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.repository.WorkspaceMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkspaceMemberService {
	private final ApplicationEventPublisher eventPublisher;

	private final WorkspaceMemberRepository workspaceMemberRepository;

	private final MemberService memberService;

	private final EmailService emailService;

	private final FileService fileService;

	public void createWorkspaceMember(Workspace workspace, Member member, WorkspaceMemberState state,
		String positionType) {
		// 워크스페이스 멤버 생성
		WorkspaceMember workspaceMember = WorkspaceMember.builder()
			.workspace(workspace)
			.member(member)
			.nickName(member.getFullName())
			.state(state)
			.positionType(positionType)
			.responsibility(null)
			.department(null)
			.profileFile(null)
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

	public List<WorkspaceMember> getAllWorkspaceMembersByMember(Member member) {
		return workspaceMemberRepository.findByMember(member);
	}

	public WorkspaceMember updateWorkspaceMemberAuthority(WorkspaceMember updatedWorkspaceMember) {
		WorkspaceMember existingWorkspaceMember = workspaceMemberRepository.findById(updatedWorkspaceMember.getId())
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_WORKSPACEMEMBER", "워크스페이스 멤버를 찾을 수 없습니다."));

		// 역할 변경 이벤트를 위해 이전 역할 저장
		String oldPositionType = existingWorkspaceMember.getPositionType();
		String newPositionType = updatedWorkspaceMember.getPositionType();

		existingWorkspaceMember.setWorkspace(updatedWorkspaceMember.getWorkspace());
		existingWorkspaceMember.setMember(updatedWorkspaceMember.getMember());
		existingWorkspaceMember.setState(updatedWorkspaceMember.getState());
		existingWorkspaceMember.setPositionType(newPositionType);

		WorkspaceMember savedMember = workspaceMemberRepository.save(existingWorkspaceMember);

		// 역할이 변경된 경우에만 이벤트 발행
		if (!oldPositionType.equals(newPositionType)) {
			log.info("워크스페이스 멤버 역할 변경: memberId={}, workspaceId={}, oldRole={}, newRole={}",
				savedMember.getMember().getId(), savedMember.getWorkspace().getId(), oldPositionType, newPositionType);

			eventPublisher.publishEvent(new WorkspaceRoleChangedEvent(
				savedMember.getWorkspace(),
				savedMember.getMember(),
				oldPositionType,
				newPositionType
			));
		}

		return savedMember;
	}

	public WorkspaceMember updateWorkspaceMemberInfo(WorkspaceMember workspaceMember) {
		WorkspaceMember existingWorkspaceMember = workspaceMemberRepository.findById(workspaceMember.getId())
			.orElseThrow(() -> new CustomException("CANNOT_FOUND_WORKSPACEMEMBER", "워크스페이스 멤버를 찾을 수 없습니다."));
		existingWorkspaceMember.setNickName(workspaceMember.getNickName());
		existingWorkspaceMember.setDepartment(workspaceMember.getDepartment());
		existingWorkspaceMember.setResponsibility(workspaceMember.getResponsibility());
		existingWorkspaceMember.setProfileFile(workspaceMember.getProfileFile());
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

			Member member = memberOptional.get();

			// 이미 삭제된 워크스페이스 멤버인지 확인
			Optional<WorkspaceMember> existingDeletedMember = workspaceMemberRepository
				.findByWorkspaceIdAndMemberId(workspace.getId(), member.getId());

			if (existingDeletedMember.isPresent()
				&& existingDeletedMember.get().getState() == WorkspaceMemberState.DELETED) {
				// 삭제된 워크스페이스 멤버 재활성화
				WorkspaceMember deletedMember = existingDeletedMember.get();
				deletedMember.setNickName(member.getFullName());
				deletedMember.setState(WorkspaceMemberState.ACTIVE);
				deletedMember.setPositionType(workspaceMemberCreateRequest.getPositionType());
				workspaceMemberRepository.save(deletedMember);

				// 워크스페이스 초대 이벤트 발행 (재활성화)
				log.info("워크스페이스 멤버 재활성화: memberId={}, workspaceId={}", member.getId(), workspace.getId());
				eventPublisher.publishEvent(new WorkspaceInviteEvent(workspace, member));
			} else if (existingDeletedMember.isEmpty()) {
				// 새 워크스페이스 멤버 생성
				WorkspaceMember workspaceMember = WorkspaceMember.builder()
					.workspace(workspace)
					.member(member)
					.nickName(member.getFullName())
					.state(workspaceMemberCreateRequest.getState())
					.positionType(workspaceMemberCreateRequest.getPositionType())
					.build();
				workspaceMemberRepository.save(workspaceMember);

				// 워크스페이스 초대 이벤트 발행 (신규 생성)
				log.info("워크스페이스 멤버 초대: memberId={}, workspaceId={}", member.getId(), workspace.getId());
				eventPublisher.publishEvent(new WorkspaceInviteEvent(workspace, member));
			}
		}
	}

	private List<WorkspaceMemberInfoResponse> buildResponse(Long workspaceId, List<String> newMemberEmailList) {
		return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
			.filter(workspaceMember -> newMemberEmailList.contains(workspaceMember.getMember().getEmail()))
			.filter(workspaceMember -> workspaceMember.getState() == WorkspaceMemberState.ACTIVE)
			.map(workspaceMember -> WorkspaceMemberInfoResponse.builder()
				.workspaceMemberid(workspaceMember.getId())
				.name(workspaceMember.getNickName())
				.email(workspaceMember.getMember().getEmail())
				.positionType(workspaceMember.getPositionType())
				.state(workspaceMember.getState())
				.department(workspaceMember.getDepartment())
				.responsibility(workspaceMember.getResponsibility())
				.profileFileUrl(workspaceMember.getProfileFile() != null ?
					fileService.getFileUrlByPath(workspaceMember.getProfileFile().getFilePath()) : null)
				.build())
			.toList();
	}

	/**
	 * 워크스페이스의 활성 멤버 수를 계산하는 메소드
	 * @param workspaceId 워크스페이스 ID
	 * @return 활성 멤버 수
	 */
	public int countActiveWorkspaceMembers(Long workspaceId) {
		return (int) workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
			.filter(workspaceMember -> workspaceMember.getState() == WorkspaceMemberState.ACTIVE)
			.count();
	}
}
