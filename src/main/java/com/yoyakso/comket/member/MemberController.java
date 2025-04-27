package com.yoyakso.comket.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MemberController {
	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	//회원가입
	// @PostMapping("/auth/register")
	// public ResponseEntity<Member> registerMember(@RequestBody Member member){
	// 	// Member registeredMember = memberService.registerMember(member);
	// 	// return ResponseEntity.ok(registeredMember);
	// }
	//
 	//회원 정보 조회
	// @GetMapping("/members/me")
	// public ResponseEntity<Member> getMember(){
	//
	// }
}
