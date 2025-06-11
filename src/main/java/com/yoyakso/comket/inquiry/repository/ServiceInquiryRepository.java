package com.yoyakso.comket.inquiry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.inquiry.entity.ServiceInquiry;

@Repository
public interface ServiceInquiryRepository extends JpaRepository<ServiceInquiry, Long> {
    // Custom query methods can be added here if needed
}