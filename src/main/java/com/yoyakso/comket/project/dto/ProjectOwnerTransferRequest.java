package com.yoyakso.comket.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectOwnerTransferRequest {
	private Long targetMemberId;
}
