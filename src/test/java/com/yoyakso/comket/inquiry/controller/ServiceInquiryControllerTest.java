package com.yoyakso.comket.inquiry.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryAnswerRequest;
import com.yoyakso.comket.inquiry.dto.request.ServiceInquiryCreateRequest;
import com.yoyakso.comket.inquiry.dto.response.ServiceInquiryResponse;
import com.yoyakso.comket.inquiry.enums.InquiryType;
import com.yoyakso.comket.inquiry.service.ServiceInquiryService;

class ServiceInquiryControllerTest {

    @Mock
    private ServiceInquiryService serviceInquiryService;

    @InjectMocks
    private ServiceInquiryController serviceInquiryController;

    private ServiceInquiryCreateRequest createRequest;
    private ServiceInquiryAnswerRequest answerRequest;
    private ServiceInquiryResponse inquiryResponse;
    private List<ServiceInquiryResponse> inquiryResponses;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test data
        createRequest = new ServiceInquiryCreateRequest();
        createRequest.setName("Test User");
        createRequest.setEmail("test@example.com");
        createRequest.setType("PRODUCT");
        createRequest.setMessage("Test message");

        answerRequest = new ServiceInquiryAnswerRequest();
        answerRequest.setAnswer("Test answer");

        inquiryResponse = ServiceInquiryResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .type(InquiryType.PRODUCT)
                .message("Test message")
                .answer("Test answer")
                .answered(true)
                .build();

        inquiryResponses = Arrays.asList(inquiryResponse);
    }

    @Test
    void testCreateInquiry_Success() {
        // Arrange
        when(serviceInquiryService.createInquiry(any(ServiceInquiryCreateRequest.class)))
                .thenReturn(inquiryResponse);

        // Act
        ResponseEntity<ServiceInquiryResponse> response = serviceInquiryController.createInquiry(createRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test User", response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals(InquiryType.PRODUCT, response.getBody().getType());
        assertEquals("Test message", response.getBody().getMessage());
        
        // Verify
        verify(serviceInquiryService).createInquiry(createRequest);
    }

    @Test
    void testGetAllInquiries_Success() {
        // Arrange
        when(serviceInquiryService.getAllInquiries()).thenReturn(inquiryResponses);

        // Act
        ResponseEntity<List<ServiceInquiryResponse>> response = serviceInquiryController.getAllInquiries();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
        
        // Verify
        verify(serviceInquiryService).getAllInquiries();
    }

    @Test
    void testGetAllInquiries_Unauthorized() {
        // Arrange
        when(serviceInquiryService.getAllInquiries())
                .thenThrow(new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다."));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
                () -> serviceInquiryController.getAllInquiries());
        assertEquals("UNAUTHORIZED", exception.getCode());
        
        // Verify
        verify(serviceInquiryService).getAllInquiries();
    }

    @Test
    void testGetInquiry_Success() {
        // Arrange
        when(serviceInquiryService.getInquiry(1L)).thenReturn(inquiryResponse);

        // Act
        ResponseEntity<ServiceInquiryResponse> response = serviceInquiryController.getInquiry(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        
        // Verify
        verify(serviceInquiryService).getInquiry(1L);
    }

    @Test
    void testGetInquiry_Unauthorized() {
        // Arrange
        when(serviceInquiryService.getInquiry(1L))
                .thenThrow(new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다."));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
                () -> serviceInquiryController.getInquiry(1L));
        assertEquals("UNAUTHORIZED", exception.getCode());
        
        // Verify
        verify(serviceInquiryService).getInquiry(1L);
    }

    @Test
    void testAnswerInquiry_Success() {
        // Arrange
        when(serviceInquiryService.answerInquiry(eq(1L), any(ServiceInquiryAnswerRequest.class)))
                .thenReturn(inquiryResponse);

        // Act
        ResponseEntity<ServiceInquiryResponse> response = serviceInquiryController.answerInquiry(1L, answerRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Test answer", response.getBody().getAnswer());
        assertTrue(response.getBody().isAnswered());
        
        // Verify
        verify(serviceInquiryService).answerInquiry(1L, answerRequest);
    }

    @Test
    void testAnswerInquiry_Unauthorized() {
        // Arrange
        when(serviceInquiryService.answerInquiry(eq(1L), any(ServiceInquiryAnswerRequest.class)))
                .thenThrow(new CustomException("UNAUTHORIZED", "관리자만 접근할 수 있습니다."));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, 
                () -> serviceInquiryController.answerInquiry(1L, answerRequest));
        assertEquals("UNAUTHORIZED", exception.getCode());
        
        // Verify
        verify(serviceInquiryService).answerInquiry(1L, answerRequest);
    }
}