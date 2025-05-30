package com.yoyakso.comket.ai.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.ai.dto.ActionItemAssigneeDto;
import com.yoyakso.comket.ai.dto.ActionItemContentDto;
import com.yoyakso.comket.ai.dto.AiSummaryWithActionItemsResponse;
import com.yoyakso.comket.ai.entity.AiActionItem;
import com.yoyakso.comket.ai.entity.AiSummary;
import com.yoyakso.comket.ai.enums.SummaryType;
import com.yoyakso.comket.ai.repository.AiActionItemsRepository;
import com.yoyakso.comket.ai.repository.AiSummaryRepository;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;
import com.yoyakso.comket.project.service.ProjectService;
import com.yoyakso.comket.projectMember.repository.ProjectMemberRepository;
import com.yoyakso.comket.projectMember.service.ProjectMemberService;
import com.yoyakso.comket.thread.entity.ThreadMessage;
import com.yoyakso.comket.thread.repository.ThreadMessageRepository;
import com.yoyakso.comket.ticket.entity.Ticket;
import com.yoyakso.comket.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

	private final TicketRepository ticketRepository;
	private final ThreadMessageRepository threadMessageRepository;
	private final OpenAiClient openAiClient;
	private final MemberService memberService;
	private final AiSummaryRepository aiSummaryRepository;
	private final AiActionItemsRepository aiActionItemsRepository;
	private final ProjectMemberRepository projectMemberRepository;
	private final ProjectMemberService projectMemberService;
	private final ProjectService projectService;

	public AiSummaryWithActionItemsResponse summarizeThread(Long ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException("TICKET_NOT_FOUND", "해당 티켓을 찾을 수 없습니다."));

		List<ThreadMessage> messages = threadMessageRepository.findAllByTicketIdOrderBySentAtAsc(ticketId);

		// 프롬프트 생성
		StringBuilder promptBuilder = new StringBuilder();
		promptBuilder.append("티켓 정보\n")
			.append("제목: ").append(ticket.getName()).append("\n")
			.append("설명: ").append(ticket.getDescription()).append("\n")
			.append("유형: ").append(ticket.getType()).append("\n")
			.append("우선순위: ").append(ticket.getPriority()).append("\n")
			.append("마감일: ").append(ticket.getEndDate() != null ? ticket.getEndDate().toString() : "없음").append("\n\n")
			.append("스레드 메시지 목록 (시간순)\n");

		for (ThreadMessage message : messages) {
			String name = memberService.findMemberNameById(message.getSenderMemberId());
			Long projectMemberId = projectMemberService.findProjectMemberIdByProjectIdAndMemberId(
				ticket.getProject().getId(),
				message.getSenderMemberId()
			);
			promptBuilder.append("{").append(name).append("-").append(projectMemberId).append("}: ")
				.append(message.getContent()).append("\n");
		}

		promptBuilder.append("\n")
			.append("아래 형식에 맞춰서 액션아이템(할 일)과 요약을 만들어줘. action item 책임자는 {이름-번호} 형식으로 명확히 표시해줘.\n")
			.append("title과 priority는 꼭 명시해주고, 스레드에 참여해서 대화를 한 적 없는 사람의 이름이 나온다면, null로 리턴해줘.\n")
			.append("action item 책임자가 있다면 책임자의의 id와 이름 쌍은 절대 틀리면 안돼. 꼭 스레드 대화에서 찾은 id와 이름의 짝을 지켜줘.\n")
			.append("만약 같은 이름을 갖는 사람들이 있다면, 이름은 그냥 똑같이 표시해줘.\n")
			.append("언제까지 완료하겠다는 대화가 없었다면, 티켓의 마감일로 리턴해줘. \n")
			.append("티켓에 마감일도 없고, 마감일에 대한 대화가 없다면 null로 리턴해줘. \n")
			.append("만약 여러개의 작업이 존재한다면, actionItems에 리스트로 여러개 만들어줘. \n")
			.append("memberId는 항상 Long 타입이어야해. \n")
			.append("응답 예시:\n")
			.append("{\n")
			.append("  \"summary\": \"티켓 생성 시 프로젝트 이름 중복으로 인한 이슈 발생\",\n")
			.append("  \"actionItems\": \"[\", \n")
			.append("	 {\n")
			.append("  		\"title\": \"프로젝트 목록 권한 서버 로직 수정\",\n")
			.append("  		\"priority\": \"MEDIUM\",\n // HIGH, MEDIUM, LOW 세가지 단계가 있음")
			.append("  		\"memberInfo\": { \"projectMemberId\": 17, \"name\": \"조민현\" },\n // 담당자를 찾기 어려우면 null")
			.append("  		\"dueDate\": \"2025-00-00\"\n // 날짜에 대한 대화가 없다면 null")
			.append("	 },\n")
			.append("	 {\n")
			.append("  		\"title\": \"프로젝트 목록 권한 에러 발생 시 에러 페이지 노출 기획 결정\",\n")
			.append("  		\"priority\": \"LOW\",\n // HIGH, MEDIUM, LOW 세가지 단계가 있음")
			.append("  		\"memberInfo\": null,\n // 담당자를 찾기 어려우면 null")
			.append("  		\"dueDate\": \"2025-00-00\"\n // 날짜에 대한 대화가 없다면 null")
			.append("	 },\n")
			.append("	]\n")
			.append("}");

		String aiResult = openAiClient.getAiSummary(promptBuilder.toString());

		ObjectMapper mapper = new ObjectMapper();
		AiSummaryWithActionItemsResponse response;
		try {
			response = mapper.readValue(aiResult, AiSummaryWithActionItemsResponse.class);
			response.setCreateTime(LocalDateTime.now());
		} catch (Exception e) {
			throw new CustomException("AI_PARSE_ERROR", "AI 응답 파싱에 실패했습니다." + e.getMessage());
		}

		AiSummary summary = AiSummary.builder()
			.ticket(ticket)
			.summaryType(SummaryType.GENERAL)
			.summary(response.getSummary())
			.build();

		AiSummary savedSummary = aiSummaryRepository.save(summary);

		for (ActionItemContentDto actionItemData : response.getActionItems()) {
			Member member = Optional.ofNullable(actionItemData.getMemberInfo())
				.map(info -> {
					Long memberId = projectMemberService.getMemberIdByProjectMemberId(info.getProjectMemberId());
					return memberService.getMemberById(memberId);
				})
				.orElse(null);

			AiActionItem item = AiActionItem.builder()
				.aiSummary(savedSummary)
				.title(actionItemData.getTitle())
				.assignee(member)
				.priority(actionItemData.getPriority())
				.dueDate(actionItemData.getDueDate())
				.build();
			aiActionItemsRepository.save(item);
		}

		return response;
	}

	public AiSummaryWithActionItemsResponse[] getSummaryHistoryAll(Long ticketId) {
		Ticket ticket = ticketRepository.findById(ticketId)
			.orElseThrow(() -> new CustomException("TICKET_NOT_FOUND", "해당 티켓을 찾을 수 없습니다."));

		List<AiSummary> summaries = aiSummaryRepository.findAllByTicketIdOrderByCreateTimeAsc(ticketId);

		List<AiSummaryWithActionItemsResponse> result = new ArrayList<>();

		for (AiSummary summary : summaries) {
			List<AiActionItem> items = aiActionItemsRepository.findAllByAiSummaryId(summary.getId());

			List<ActionItemContentDto> itemDtos = items.stream()
				.map(item -> {
					ActionItemAssigneeDto memberInfo = null;
					if (item.getAssignee() != null) {
						Long memberId = item.getAssignee().getId();
						Member member = memberService.getMemberById(memberId);

						Long projectMemberId = projectMemberService.findProjectMemberIdByProjectIdAndMemberId(
							ticket.getProject().getId(),
							memberId
						);

						memberInfo = ActionItemAssigneeDto.builder()
							.projectMemberId(projectMemberId)
							.name(member.getRealName())
							.build();
					}

					return ActionItemContentDto.builder()
						.title(item.getTitle())
						.priority(item.getPriority())
						.memberInfo(memberInfo)
						.dueDate(item.getDueDate())
						.build();
				})
				.collect(Collectors.toList());

			AiSummaryWithActionItemsResponse dto = AiSummaryWithActionItemsResponse.builder()
				.summary(summary.getSummary())
				.actionItems(itemDtos.toArray(new ActionItemContentDto[0]))
				.createTime(summary.getCreateTime())
				.build();

			result.add(dto);
		}

		return result.toArray(new AiSummaryWithActionItemsResponse[0]);
	}

}