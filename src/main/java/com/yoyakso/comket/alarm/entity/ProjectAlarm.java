package com.yoyakso.comket.alarm.entity;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAlarm {

	private Member member; // Member 엔티티를 사용한다고 가정

	private Project project;

	private Long count = 0L;

	private String alarmMessage;
}
