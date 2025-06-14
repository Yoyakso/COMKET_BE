package com.yoyakso.comket.ticket.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.ticket.enums.TicketPriority;
import com.yoyakso.comket.ticket.enums.TicketState;
import com.yoyakso.comket.ticket.enums.TicketType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {

	@NotBlank(message = "Name must not be blank")
	@JsonProperty("ticket_name")
	private String name;

	private String description;

	@JsonProperty("ticket_type")
	private TicketType type;

	@JsonProperty("parent_ticket_id")
	private Long parentTicketId;

	@JsonProperty("ticket_priority")
	private TicketPriority priority;

	@JsonProperty("ticket_state")
	@NotNull(message = "Ticket state is required")
	private TicketState state;

	@JsonProperty("start_date")
	private LocalDate startDate;

	@JsonProperty("end_date")
	private LocalDate endDate;

	@JsonProperty("assignee_member_id_list")
	private List<Long> assigneeIdList;

	@JsonProperty("additional_info")
	private Map<String, Object> additionalInfo; // 템플릿별 추가 정보

	@JsonCreator
	public void setPriority(@JsonProperty("ticket_priority") String priority) {
		this.priority = (priority != null) ? TicketPriority.valueOf(priority.toUpperCase()) : null;
	}

	@JsonCreator
	public void setState(@JsonProperty("ticket_state") String state) {
		this.state = (state != null) ? TicketState.valueOf(state.toUpperCase()) : null;
	}
}