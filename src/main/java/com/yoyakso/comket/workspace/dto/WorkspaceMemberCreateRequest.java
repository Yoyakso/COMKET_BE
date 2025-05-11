package com.yoyakso.comket.workspace.dto;

import java.util.List;

import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberCreateRequest {
	private List<String> memberEmailList;
	private String positionType;
	private WorkspaceMemberState state;
}
