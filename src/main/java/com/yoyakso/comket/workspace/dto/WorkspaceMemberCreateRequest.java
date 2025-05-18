package com.yoyakso.comket.workspace.dto;

import java.util.List;

import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberCreateRequest {
	private List<String> memberEmailList;
	private String positionType;
	private WorkspaceMemberState state;
}
