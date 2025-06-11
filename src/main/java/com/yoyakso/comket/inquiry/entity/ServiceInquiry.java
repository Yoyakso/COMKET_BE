package com.yoyakso.comket.inquiry.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.yoyakso.comket.inquiry.enums.InquiryType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "service_inquiry")
public class ServiceInquiry {

    // 문의 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이름
    @NotNull
    private String name;

    // 이메일
    @NotNull
    @Email
    private String email;

    // 문의 유형
    @NotNull
    @Enumerated(EnumType.STRING)
    private InquiryType type;

    // 문의 메시지
    @NotNull
    @Lob
    private String message;

    // 답변
    @Lob
    private String answer;

    // 답변 여부
    private boolean answered = false;

    // 생성 일자
    @CreationTimestamp
    private LocalDateTime createdAt;

    // 업데이트 일자
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}