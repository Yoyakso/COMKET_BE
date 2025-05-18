package com.yoyakso.comket.ticket.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDeleteRequest {
	@NotNull
	@JsonProperty("ticket_ids")
	private List<Long> ticketIds;
}
