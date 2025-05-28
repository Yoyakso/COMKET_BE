package com.yoyakso.comket.ai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoyakso.comket.ai.dto.AiSummaryWithActionItemsResponse;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.repository.MemberRepository;
import com.yoyakso.comket.member.service.MemberService;
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
	private final MemberRepository memberRepository;
	private final OpenAiClient openAiClient;
	private final MemberService memberService;

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
			promptBuilder.append("{").append(name).append("-").append(message.getSenderMemberId()).append("}: ")
				.append(message.getContent()).append("\n");

			System.out.println(
				"[MESSAGE] - " + "[" + message.getSenderMemberId() + "] " + name + ": " + message.getContent()
			);
		}

		promptBuilder.append("\n")
			.append("아래 형식에 맞춰서 액션아이템(할 일)과 요약을 만들어줘. action item 책임자는 {이름-번호} 형식으로 명확히 표시해줘.\n")
			.append("title과 priority는 꼭 명시해주고, 스레드에 참여해서 대화를 한 적 없는 사람의 이름이 나온다면, null로 리턴해줘.\n")
			.append("action item 책임자가 있다면 책임자의의 id와 이름 쌍은 절대 틀리면 안돼. 꼭 스레드 대화에서 찾은 id와 이름의 짝을 지켜줘.\n")
			.append("언제까지 완료하겠다는 대화가 없었다면, null로 리턴해줘. \n")
			.append("만약 여러개의 작업이 존재한다면, actionItems에 리스트로 여러개 만들어줘. \n")
			.append("응답 예시:\n")
			.append("{\n")
			.append("  \"summary\": \"프로젝트 목록 불러오기 버그와 관련해 아래 액션아이템이 도출됨.\",\n")
			.append("  \"actionItems\": \"[\", \n")
			.append("	 {\n")
			.append("  		\"title\": \"프로젝트 목록 권한 서버 로직 수정\",\n")
			.append("  		\"priority\": \"MEDIUM\",\n // HIGH, MEDIUM, LOW 세가지 단계가 있음")
			.append("  		\"memberInfo\": { \"memberId\": 17, \"name\": \"조민현\" },\n // 담당자를 찾기 어려우면 null")
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

		System.out.println("[aiResult]: " + aiResult);

		ObjectMapper mapper = new ObjectMapper();
		AiSummaryWithActionItemsResponse response;
		try {
			response = mapper.readValue(aiResult, AiSummaryWithActionItemsResponse.class);
		} catch (Exception e) {
			throw new CustomException("AI_PARSE_ERROR", "AI 응답 파싱에 실패했습니다." + e.getMessage());
		}
		return response;
	}
}

/*
#1
[aiResult]: {
  "response": "티켓 생성 시 프로젝트 이름 중복 이슈에 대한 아래 액션아이템이 도출됨.",
  "title": "프로젝트 중복 검증 로직 수정",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 2, "name": "이태경" },
  "dueDate": "2025-05-25"
}

#2
[aiResult]: {
  "response": "티켓 생성 시 프로젝트 중복 이슈에 대한 아래 액션아이템이 도출됨.",
  "title": "프로젝트 중복 이슈 해결",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 2, "name": "이태경" },
  "dueDate": "2025-05-27"
}

#3 프로젝트 담당자 변경 메시지 전송 후
[aiResult]: {
  "response": "티켓 생성 중 프로젝트 이름 중복 이슈에 대한 아래 액션아이템이 도출되었습니다.",
  "title": "프로젝트 이름 중복 이슈 해결",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 2, "name": "이태경" },
  "dueDate": "2025-05-27"
}

#4 태경님 확인 후
[aiResult]: {
  "response": "티켓 생성 시 프로젝트 중복 이슈 관련하여 아래 액션아이템이 도출됨.",
  "title": "티켓 생성 시 프로젝트 중복 이슈 해결",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 2, "name": "이태경" },
  "dueDate": "2025-05-27"
}

#5 '민현'님이 작업 한다고 명시
[aiResult]: {
  "response": "티켓 생성 시 프로젝트 이름 중복 이슈에 대한 액션아이템과 요약입니다.",
  "title": "프로젝트 이름 중복 이슈 해결",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 2, "name": "이태경" },
  "dueDate": "2025-05-25"
}

#6 GPT-4.0 사용
[aiResult]: {
  "summary": "티켓 생성 시 프로젝트 이름 중복 이슈 발생. 워크스페이스를 조건으로 추가하여 수정하는 작업 필요.",
  "title": "티켓 생성 시 프로젝트 이름 중복 이슈 수정",
  "priority": "MEDIUM",
  "assigneeMember": { "id": 1, "name": "조민현" },
  "dueDate": null
}

#7 액션아이템 여러개 테스트
[MESSAGE] - [2] 이태경: 안녕하세요.
[MESSAGE] - [2] 이태경: 민현님, 이슈 공유 감사합니다. 확인해 보겠습니다.
[MESSAGE] - [1] 조민현: 넵, 감사합니다.
[MESSAGE] - [2] 이태경: 민현님, 이슈 확인했습니다. 해당 이슈는 티켓 생성 시 프로젝트 정보를 조회하는 과정에서 발생했습니다.
[MESSAGE] - [2] 이태경: 프로젝트 정보 조회 시 프로젝트 이름으로 프로젝트를 검증하는데, 프로젝트 테이블 전체에서 이름을 조회하여 다른 워크스페이스의 프로젝트인 경우에도 조회를 하다 보니 DB에서 어떤 값을 리턴해야하는지 몰라 에러가 발생했습니다.
[MESSAGE] - [2] 이태경: 따라서 이 부분 워크스페이스를 조건으로 추가하도록 작업해서 PR올리겠습니다.
[MESSAGE] - [1] 조민현: 넵, 감사합니다. 하위 티켓 생성해서 작업 후 전달주세요.
[MESSAGE] - [1] 조민현: 태경님, 다시 생각해보니 작업은 제가 하는게 좋을 것 같습니다.
[MESSAGE] - [2] 이태경: 넵 알겠습니다.
[MESSAGE] - [2] 이태경: 그럼 민현님이 작업 해주시는 것으로 알고 정리하겠습니다.
[MESSAGE] - [1] 조민현: 넵, 이슈 발생 시 에러 메시지가 노출되지 않고 있습니다. 이 부분 기획자 확인이 필요합니다.
[MESSAGE] - [1] 조민현: 이 부분 기획자 수연님께 전달 드려 확인 부탁드리도록 하겠습니다.
[aiResult]: {
  "summary": "티켓 생성 시 프로젝트 이름 중복으로 인한 이슈가 발생하였고, 아래 액션아이템이 도출되었습니다.",
  "actionItems": [
	 {
  		"title": "프로젝트 정보 조회 로직 수정",
  		"priority": "MEDIUM",
  		"memberInfo": { "memberId": 1, "name": "조민현" },
  		"dueDate": null
	 },
	 {
  		"title": "이슈 발생 시 에러 메시지 노출 여부 확인",
  		"priority": "MEDIUM",
  		"memberInfo": null,
  		"dueDate": null
	 }
  ]
}
 */