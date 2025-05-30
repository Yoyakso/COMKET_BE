package com.yoyakso.comket.alarm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alarm")
public class Alarm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 멤버
	@Column(nullable = false)
	private Long memberId;

	// 프로젝트 ID
	@Column(nullable = false)
	private Long projectId;

	// // 티켓 ID
	// 티켓 별로 알림을 받게 될 경우, DB에 너무 많은 정보가 쌓일 수 있다.
	// 따라서, 멤버별, 티켓별 알림을 표기하기 위해서는 Redis를 활용해야할 것으로 보이는데,
	// 이는 일단 추후에 표기하는 것으로 하는 것이 옳다고 판단되어 주석 처리함.
	// @Column(nullable = false)
	// private Long ticketId;

	// 알림 수 Count
	@Column(nullable = false)
	private Long count = 0L;
}
