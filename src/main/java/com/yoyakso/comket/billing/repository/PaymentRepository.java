package com.yoyakso.comket.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yoyakso.comket.billing.entity.Payment;
import com.yoyakso.comket.workspace.entity.Workspace;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByImpUid(String impUid);
    Optional<Payment> findByWorkspace(Workspace workspace);
}
