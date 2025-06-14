package com.yoyakso.comket.ticket.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.project.entity.Project;
import com.yoyakso.comket.ticket.converter.AdditionalInfoConverter;
import com.yoyakso.comket.ticket.enums.TicketPriority;
import com.yoyakso.comket.ticket.enums.TicketState;
import com.yoyakso.comket.ticket.enums.TicketType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "ticket")
public class Ticket {

	//티켓 ID
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//티켓 이름
	@NotNull
	private String name;

	//티켓 설명
	@Lob
	private String description;

	//티켓 타입
	@Enumerated(EnumType.STRING)
	private TicketType type;

	//프로젝트
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	//부모 티켓
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_ticket_id")
	private Ticket parentTicket;

	@ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(
		name = "ticket_assignee",
		joinColumns = @JoinColumn(name = "ticket_id"),
		inverseJoinColumns = @JoinColumn(name = "member_id"),
		uniqueConstraints = @UniqueConstraint(columnNames = {"ticket_id", "member_id"})
	)
	private List<Member> assignees = new ArrayList<>();

	//우선순위
	@Enumerated(EnumType.STRING)
	private TicketPriority priority;

	//상태
	@Enumerated(EnumType.STRING)
	private TicketState state;

	//시작 일자
	private LocalDate startDate;

	//종료 일자
	private LocalDate endDate;

	//생성 일자
	@CreationTimestamp
	private LocalDateTime createdAt;

	//업데이트 일자
	@UpdateTimestamp
	private LocalDateTime updatedAt;

	//작성자
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_member_id", nullable = false)
	private Member creator;

	//삭제 여부
	//티켓은 삭제여부만 존재하기에 isDeleted로 명명
	private boolean isDeleted = false;

	@Lob
	@Convert(converter = AdditionalInfoConverter.class)
	private Map<String, Object> additionalInfo;

	@Transient
	private Long subTicketCount = 0L; // 서브 티켓 개수
}
