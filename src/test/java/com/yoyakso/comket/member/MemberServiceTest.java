package com.yoyakso.comket.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.member.dto.MemberRegisterResponse;
import com.yoyakso.comket.member.dto.MemberUpdateRequest;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.util.JwtTokenProvider;

class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private MemberService memberService;

	private Member testMember;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testMember = createTestMember();
	}

	@AfterEach
	void tearDown() {
		// 테스트 후 데이터 정리
		if (testMember.getId() != null) {
			memberRepository.delete(testMember);
		}
	}

	@Test
	void testRegisterMember_Success() {
		when(memberRepository.existsByEmail(testMember.getEmail())).thenReturn(false);
		when(memberRepository.existsByNickname(testMember.getNickname())).thenReturn(false);
		when(passwordEncoder.encode(testMember.getPassword())).thenReturn("encodedPassword");
		when(jwtTokenProvider.createToken(testMember.getEmail())).thenReturn("jwtToken");
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member savedMember = invocation.getArgument(0);
			savedMember.setId(1L);
			return savedMember;
		});

		MemberRegisterResponse response = memberService.registerMember(testMember);

		assertNotNull(response);
		assertEquals(1L, response.getMemberId());
		assertEquals("test@example.com", response.getEmail());
		assertEquals("testUser", response.getNickname());
		assertEquals("jwtToken", response.getToken());
	}

	@Test
	void testRegisterMember_EmailDuplicate() {
		when(memberRepository.existsByEmail(testMember.getEmail())).thenReturn(true);

		CustomException exception = assertThrows(CustomException.class, () -> memberService.registerMember(testMember));
		assertEquals("EMAIL_DUPLICATE", exception.getCode());
	}

	@Test
	void testDeleteMember_Success() {
		when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(testMember);

		assertDoesNotThrow(() -> memberService.deleteMember(testMember.getEmail()));
		verify(memberRepository, times(1)).delete(testMember);
	}

	@Test
	void testDeleteMember_NotFound() {
		when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

		CustomException exception = assertThrows(CustomException.class,
			() -> memberService.deleteMember("nonexistent@example.com"));
		assertEquals("MEMBER_NOT_FOUND", exception.getCode());
	}

	@Test
	void testUpdateMember_Success() {
		MemberUpdateRequest updateRequest = new MemberUpdateRequest();
		updateRequest.setNickname("updatedNickname");
		updateRequest.setRealName("Updated Real Name");

		when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(false);
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Member updatedMember = memberService.updateMember(testMember, updateRequest);

		assertNotNull(updatedMember);
		assertEquals("updatedNickname", updatedMember.getNickname());
		assertEquals("Updated Real Name", updatedMember.getRealName());
	}

	@Test
	void testUpdateMember_NicknameDuplicate() {
		MemberUpdateRequest updateRequest = new MemberUpdateRequest();
		updateRequest.setNickname("duplicateNickname");

		when(memberRepository.existsByNickname(updateRequest.getNickname())).thenReturn(true);

		CustomException exception = assertThrows(CustomException.class,
			() -> memberService.updateMember(testMember, updateRequest));
		assertEquals("NICKNAME_DUPLICATE", exception.getCode());
	}

	@Test
	void testFindByEmail_Success() {
		when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(testMember);

		Member foundMember = memberService.findByEmail(testMember.getEmail());

		assertNotNull(foundMember);
		assertEquals(testMember.getEmail(), foundMember.getEmail());
		assertEquals(testMember.getNickname(), foundMember.getNickname());
	}

	@Test
	void testFindByEmail_NotFound() {
		when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

		Member foundMember = memberService.findByEmail("nonexistent@example.com");

		assertNull(foundMember);
	}

	private Member createTestMember() {
		Member member = new Member();
		member.setEmail("test@example.com");
		member.setNickname("testUser");
		member.setPassword("password");
		return member;
	}
}