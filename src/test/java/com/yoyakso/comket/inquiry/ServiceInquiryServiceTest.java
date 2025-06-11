package com.yoyakso.comket.inquiry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yoyakso.comket.email.service.EmailService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryAnswerRequest;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryCreateRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.entity.ServiceInquiry;
import com.yoyakso.comket.inquiry.enums.InquiryType;
import com.yoyakso.comket.inquiry.mapper.ServiceInquiryMapper;
import com.yoyakso.comket.inquiry.repository.ServiceInquiryRepository;
import com.yoyakso.comket.inquiry.service.ServiceInquiryService;
import com.yoyakso.comket.member.entity.Member;
import com.yoyakso.comket.member.service.MemberService;

class ServiceInquiryServiceTest {

    @Mock
    private ServiceInquiryRepository serviceInquiryRepository;

    @Mock
    private ServiceInquiryMapper serviceInquiryMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ServiceInquiryService serviceInquiryService;

    private ServiceInquiry testInquiry;
    private ServiceInquiryCreateRequest testCreateRequest;
    private ServiceInquiryResponse testResponse;
    private Member adminMember;
    private Member nonAdminMember;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testInquiry = createTestInquiry();
        testCreateRequest = createTestCreateRequest();
        testResponse = createTestResponse();
        adminMember = createAdminMember();
        nonAdminMember = createNonAdminMember();
    }

    @Test
    void testCreateInquiry_Success() {
        // Arrange
        when(serviceInquiryMapper.toEntity(testCreateRequest)).thenReturn(testInquiry);
        when(serviceInquiryRepository.save(testInquiry)).thenReturn(testInquiry);
        when(serviceInquiryMapper.toResponse(testInquiry)).thenReturn(testResponse);

        // Act
        ServiceInquiryResponse response = serviceInquiryService.createInquiry(testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(InquiryType.PRODUCT, response.getType());
        assertEquals("Test message", response.getMessage());

        // Verify
        verify(serviceInquiryMapper).toEntity(testCreateRequest);
        verify(serviceInquiryRepository).save(testInquiry);
        verify(serviceInquiryMapper).toResponse(testInquiry);
    }

    @Test
    void testGetAllInquiries_AdminSuccess() {
        // Arrange
        List<ServiceInquiry> inquiries = Arrays.asList(testInquiry);
        when(memberService.getAuthenticatedMember()).thenReturn(adminMember);
        when(serviceInquiryRepository.findAll()).thenReturn(inquiries);
        when(serviceInquiryMapper.toResponse(testInquiry)).thenReturn(testResponse);

        // Act
        List<ServiceInquiryResponse> responses = serviceInquiryService.getAllInquiries();

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository).findAll();
        verify(serviceInquiryMapper).toResponse(testInquiry);
    }

    @Test
    void testGetAllInquiries_NonAdminFails() {
        // Arrange
        when(memberService.getAuthenticatedMember()).thenReturn(nonAdminMember);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
            () -> serviceInquiryService.getAllInquiries());
        assertEquals("UNAUTHORIZED", exception.getCode());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository, never()).findAll();
    }

    @Test
    void testGetInquiry_AdminSuccess() {
        // Arrange
        when(memberService.getAuthenticatedMember()).thenReturn(adminMember);
        when(serviceInquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        when(serviceInquiryMapper.toResponse(testInquiry)).thenReturn(testResponse);

        // Act
        ServiceInquiryResponse response = serviceInquiryService.getInquiry(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository).findById(1L);
        verify(serviceInquiryMapper).toResponse(testInquiry);
    }

    @Test
    void testGetInquiry_NonAdminFails() {
        // Arrange
        when(memberService.getAuthenticatedMember()).thenReturn(nonAdminMember);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
            () -> serviceInquiryService.getInquiry(1L));
        assertEquals("UNAUTHORIZED", exception.getCode());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository, never()).findById(anyLong());
    }

    @Test
    void testGetInquiry_NotFound() {
        // Arrange
        when(memberService.getAuthenticatedMember()).thenReturn(adminMember);
        when(serviceInquiryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
            () -> serviceInquiryService.getInquiry(999L));
        assertEquals("INQUIRY_NOT_FOUND", exception.getCode());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository).findById(999L);
    }

    @Test
    void testAnswerInquiry_AdminSuccess() {
        // Arrange
        ServiceInquiryAnswerRequest answerRequest = new ServiceInquiryAnswerRequest();
        answerRequest.setAnswer("Test answer");

        ServiceInquiry inquiryToAnswer = createTestInquiry();
        inquiryToAnswer.setAnswered(false);

        ServiceInquiry answeredInquiry = createTestInquiry();
        answeredInquiry.setAnswer("Test answer");
        answeredInquiry.setAnswered(true);

        ServiceInquiryResponse answeredResponse = createTestResponse();
        answeredResponse.setAnswer("Test answer");
        answeredResponse.setAnswered(true);

        when(memberService.getAuthenticatedMember()).thenReturn(adminMember);
        when(serviceInquiryRepository.findById(1L)).thenReturn(Optional.of(inquiryToAnswer));
        when(serviceInquiryRepository.save(inquiryToAnswer)).thenReturn(answeredInquiry);
        when(serviceInquiryMapper.toResponse(answeredInquiry)).thenReturn(answeredResponse);
        doNothing().when(emailService).sendInquiryAnswerEmail(any(ServiceInquiry.class));

        // Act
        ServiceInquiryResponse response = serviceInquiryService.answerInquiry(1L, answerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test answer", response.getAnswer());
        assertTrue(response.isAnswered());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository).findById(1L);
        verify(serviceInquiryRepository).save(inquiryToAnswer);
        verify(emailService).sendInquiryAnswerEmail(any(ServiceInquiry.class));
        verify(serviceInquiryMapper).toResponse(answeredInquiry);
    }

    @Test
    void testAnswerInquiry_NonAdminFails() {
        // Arrange
        ServiceInquiryAnswerRequest answerRequest = new ServiceInquiryAnswerRequest();
        answerRequest.setAnswer("Test answer");

        when(memberService.getAuthenticatedMember()).thenReturn(nonAdminMember);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
            () -> serviceInquiryService.answerInquiry(1L, answerRequest));
        assertEquals("UNAUTHORIZED", exception.getCode());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository, never()).findById(anyLong());
    }

    @Test
    void testAnswerInquiry_AlreadyAnswered() {
        // Arrange
        ServiceInquiryAnswerRequest answerRequest = new ServiceInquiryAnswerRequest();
        answerRequest.setAnswer("Test answer");

        ServiceInquiry alreadyAnsweredInquiry = createTestInquiry();
        alreadyAnsweredInquiry.setAnswered(true);
        alreadyAnsweredInquiry.setAnswer("Previous answer");

        when(memberService.getAuthenticatedMember()).thenReturn(adminMember);
        when(serviceInquiryRepository.findById(1L)).thenReturn(Optional.of(alreadyAnsweredInquiry));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
            () -> serviceInquiryService.answerInquiry(1L, answerRequest));
        assertEquals("ALREADY_ANSWERED", exception.getCode());

        // Verify
        verify(memberService).getAuthenticatedMember();
        verify(serviceInquiryRepository).findById(1L);
        verify(serviceInquiryRepository, never()).save(any(ServiceInquiry.class));
    }

    private ServiceInquiry createTestInquiry() {
        ServiceInquiry inquiry = new ServiceInquiry();
        inquiry.setId(1L);
        inquiry.setName("Test User");
        inquiry.setEmail("test@example.com");
        inquiry.setType(InquiryType.PRODUCT);
        inquiry.setMessage("Test message");
        return inquiry;
    }

    private ServiceInquiryCreateRequest createTestCreateRequest() {
        ServiceInquiryCreateRequest request = new ServiceInquiryCreateRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setType("PRODUCT");
        request.setMessage("Test message");
        return request;
    }

    private ServiceInquiryResponse createTestResponse() {
        return ServiceInquiryResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .type(InquiryType.PRODUCT)
                .message("Test message")
                .answered(false)
                .build();
    }

    private Member createAdminMember() {
        Member member = new Member();
        member.setId(1L);
        member.setEmail("admin@example.com");
        member.setFullName("Admin User");
        member.setIsAdmin(true);
        return member;
    }

    private Member createNonAdminMember() {
        Member member = new Member();
        member.setId(2L);
        member.setEmail("user@example.com");
        member.setFullName("Regular User");
        member.setIsAdmin(false);
        return member;
    }
}
