package com.yoyakso.comket.member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.yoyakso.comket.auth.service.RefreshTokenService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.jwt.JwtTokenProvider;
import com.yoyakso.comket.member.dto.request.MemberRegisterRequest;
import com.yoyakso.comket.member.dto.request.MemberUpdateRequest;
import com.yoyakso.comket.member.dto.response.MemberRegisterResponse;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.mapper.MemberMapper;
import com.yoyakso.comket.member.repository.MemberRepository;
import com.yoyakso.comket.member.service.MemberService;

class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private MemberMapper memberMapper;

	@Mock
	private RefreshTokenService refreshTokenService;

	@InjectMocks
	private MemberService memberService;

	private Member testMember;
	private MemberRegisterRequest testMemberRegisterRequest;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testMemberRegisterRequest = createTestMemberRegisterRequest();
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
		when(passwordEncoder.encode(testMember.getPassword())).thenReturn("encodedPassword");
		when(jwtTokenProvider.createAccessToken(testMember.getEmail())).thenReturn("jwtAccessToken");
		when(jwtTokenProvider.createRefreshToken(testMember.getEmail())).thenReturn("jwtRefreshToken");
		when(memberMapper.toEntity(any(MemberRegisterRequest.class))).thenReturn(testMember);
		when(memberMapper.toMemberRegisterResponse(any(Member.class), anyString()))
			.thenAnswer(invocation -> {
				Member member = invocation.getArgument(0);
				String accessToken = invocation.getArgument(1);
				return MemberRegisterResponse.builder()
					.memberId(member.getId())
					.email(member.getEmail())
					.accessToken(accessToken)
					.profileFileUrl(null)
					.build();
			});
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
			Member savedMember = invocation.getArgument(0);
			savedMember.setId(1L);
			return savedMember;
		});

		MemberRegisterRequest request = new MemberRegisterRequest();
		request.setEmail(testMember.getEmail());
		request.setPassword(testMember.getPassword());
		request.setFullName(testMember.getFullName());

		MemberRegisterResponse response = memberService.registerMember(request);

		assertNotNull(response);
		assertEquals(1L, response.getMemberId());
		assertEquals("test@example.com", response.getEmail());
		assertEquals("jwtAccessToken", response.getAccessToken());
		assertNull(response.getProfileFileUrl()); // 프로필 파일 URL이 null인지 확인
	}

	@Test
	void testRegisterMember_EmailDuplicate() {
		when(memberRepository.existsByEmail(testMemberRegisterRequest.getEmail())).thenReturn(true);

		CustomException exception = assertThrows(CustomException.class,
			() -> memberService.registerMember(testMemberRegisterRequest));
		assertEquals("EMAIL_DUPLICATE", exception.getCode());
	}

	@Test
	void testDeleteMember_Success() {
		when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));

		assertDoesNotThrow(() -> memberService.deleteMember(testMember));
		verify(memberRepository, times(1)).save(testMember); // soft delete 처리 확인
		assertTrue(testMember.getIsDeleted()); // 삭제 플래그 확인
	}

	@Test
	void testDeleteMember_NotFound() {
		when(memberRepository.findById(999L)).thenReturn(Optional.empty());

		CustomException exception = assertThrows(CustomException.class,
			() -> memberService.deleteMember(memberService.getMemberById(999L)));
		assertEquals("MEMBER_NOT_FOUND", exception.getCode());
	}

	@Test
	void testUpdateMember_Success() {
		MemberUpdateRequest updateRequest = new MemberUpdateRequest();
		updateRequest.setFullName("Updated Full Name");

		// memberMapper Mock 설정
		doAnswer(invocation -> {
			Member member = invocation.getArgument(0);
			MemberUpdateRequest request = invocation.getArgument(1);
			member.setFullName(request.getFullName());
			return null;
		}).when(memberMapper).updateMemberFromRequest(any(Member.class), any(MemberUpdateRequest.class));

		// memberRepository Mock 설정
		when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Member updatedMember = memberService.updateMember(testMember, updateRequest);

		assertNotNull(updatedMember);
		assertEquals("Updated Full Name", updatedMember.getFullName());
	}

	@Test
	void testFindByEmail_Success() {
		when(memberRepository.findByEmail(testMember.getEmail())).thenReturn(Optional.ofNullable(testMember));

		Member foundMember = memberService.getMemberByEmail(testMember.getEmail());

		assertNotNull(foundMember);
		assertEquals(testMember.getEmail(), foundMember.getEmail());
	}

	@Test
	void testFindByEmail_NotFound() {
		when(memberRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

		Optional<Member> foundMember = memberService.getMemberByEmailOptional("nonexistent@example.com");

		assertTrue(foundMember.isEmpty());
	}

	private Member createTestMember() {
		Member member = new Member();
		member.setId(1L);
		member.setEmail("test@example.com");
		member.setPassword("password");
		member.setFullName("Test User"); // fullName 추가
		return member;
	}

	private MemberRegisterRequest createTestMemberRegisterRequest() {
		MemberRegisterRequest request = new MemberRegisterRequest();
		request.setEmail("test@exmaple.com");
		request.setPassword("password");
		request.setFullName("Test User");
		return request;
	}
}