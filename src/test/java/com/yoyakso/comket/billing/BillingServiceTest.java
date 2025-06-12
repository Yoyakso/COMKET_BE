package com.yoyakso.comket.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yoyakso.comket.billing.entity.WorkspaceBilling;
import com.yoyakso.comket.billing.entity.WorkspacePlan;
import com.yoyakso.comket.billing.enums.BillingPlan;
import com.yoyakso.comket.billing.repository.WorkspaceBillingRepository;
import com.yoyakso.comket.billing.repository.WorkspacePlanRepository;
import com.yoyakso.comket.billing.service.BillingService;
import com.yoyakso.comket.billing.service.PaymentService;
import com.yoyakso.comket.exception.CustomException;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.event.WorkspaceCreatedEvent;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

class BillingServiceTest {

    @Mock
    private WorkspaceBillingRepository workspaceBillingRepository;

    @Mock
    private WorkspacePlanRepository workspacePlanRepository;

    @Mock
    private WorkspaceMemberService workspaceMemberService;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private BillingService billingService;

    private Workspace testWorkspace;
    private WorkspacePlan testWorkspacePlan;
    private List<WorkspaceMember> testWorkspaceMembers;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup test data
        testWorkspace = new Workspace();
        testWorkspace.setId(1L);
        testWorkspace.setName("Test Workspace");

        testWorkspacePlan = WorkspacePlan.builder()
                .id(1L)
                .workspace(testWorkspace)
                .currentPlan(BillingPlan.BASIC)
                .build();

        // Create 3 active workspace members
        testWorkspaceMembers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WorkspaceMember member = WorkspaceMember.builder()
                    .id((long) (i + 1))
                    .workspace(testWorkspace)
                    .state(WorkspaceMemberState.ACTIVE)
                    .build();
            testWorkspaceMembers.add(member);
        }
    }

    @Test
    void testHandleWorkspaceCreatedEvent() {
        // Arrange
        WorkspaceCreatedEvent event = new WorkspaceCreatedEvent(testWorkspace);
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers);
        when(workspacePlanRepository.save(any(WorkspacePlan.class))).thenReturn(testWorkspacePlan);

        // Act
        billingService.handleWorkspaceCreatedEvent(event);

        // Assert
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(workspacePlanRepository).save(any(WorkspacePlan.class));
    }

    @Test
    void testInitializeWorkspacePlan() {
        // Arrange
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers);
        when(workspacePlanRepository.save(any(WorkspacePlan.class))).thenReturn(testWorkspacePlan);

        // Act
        WorkspacePlan result = billingService.initializeWorkspacePlan(testWorkspace);

        // Assert
        assertNotNull(result);
        assertEquals(BillingPlan.BASIC, result.getCurrentPlan());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(workspacePlanRepository).save(any(WorkspacePlan.class));
    }

    @Test
    void testGetWorkspacePlan() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));

        // Act
        WorkspacePlan result = billingService.getWorkspacePlan(testWorkspace.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testWorkspacePlan.getId(), result.getId());
        assertEquals(testWorkspacePlan.getCurrentPlan(), result.getCurrentPlan());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testGetWorkspacePlan_NotFound() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            billingService.getWorkspacePlan(999L);
        });
        assertEquals("PLAN_NOT_FOUND", exception.getCode());
        verify(workspacePlanRepository).findByWorkspaceId(999L);
    }

    @Test
    void testUpdateWorkspacePlan_NoChange() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers);
        when(workspacePlanRepository.save(testWorkspacePlan)).thenReturn(testWorkspacePlan);

        // Act
        WorkspacePlan result = billingService.updateWorkspacePlan(testWorkspace.getId(), 0);

        // Assert
        assertNotNull(result);
        assertEquals(BillingPlan.BASIC, result.getCurrentPlan());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(workspacePlanRepository).save(testWorkspacePlan);
    }

    @Test
    void testUpdateWorkspacePlan_WithChange() {
        // Arrange
        // Create a plan that will change from BASIC to STARTUP when adding 3 members
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(true);
        when(workspacePlanRepository.save(any(WorkspacePlan.class))).thenReturn(
                WorkspacePlan.builder()
                        .id(1L)
                        .workspace(testWorkspace)
                        .currentPlan(BillingPlan.STARTUP)
                        .build()
        );

        // Act
        WorkspacePlan result = billingService.updateWorkspacePlan(testWorkspace.getId(), 3); // Add 3 more members

        // Assert
        assertNotNull(result);
        assertEquals(BillingPlan.STARTUP, result.getCurrentPlan());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
        verify(workspacePlanRepository).save(any(WorkspacePlan.class));
    }

    @Test
    void testUpdateWorkspacePlan_PaymentRequired() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            billingService.updateWorkspacePlan(testWorkspace.getId(), 3); // Add 3 more members
        });
        assertEquals("PAYMENT_REQUIRED", exception.getCode());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
    }

    @Test
    void testChangeBillingPlan() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspacePlanRepository.save(any(WorkspacePlan.class))).thenReturn(
                WorkspacePlan.builder()
                        .id(1L)
                        .workspace(testWorkspace)
                        .currentPlan(BillingPlan.STARTUP)
                        .build()
        );
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(true);

        // Act
        WorkspacePlan result = billingService.changeBillingPlan(testWorkspace.getId(), BillingPlan.STARTUP);

        // Assert
        assertNotNull(result);
        assertEquals(BillingPlan.STARTUP, result.getCurrentPlan());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
        verify(workspacePlanRepository).save(any(WorkspacePlan.class));
    }

    @Test
    void testChangeBillingPlan_PaymentRequired() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            billingService.changeBillingPlan(testWorkspace.getId(), BillingPlan.STARTUP);
        });
        assertEquals("PAYMENT_REQUIRED", exception.getCode());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
    }

    @Test
    void testCheckIfPlanChangeRequired_True() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members

        // Act
        boolean result = billingService.checkIfPlanChangeRequired(testWorkspace.getId(), 3); // Add 3 more members

        // Assert
        assertTrue(result);
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testCheckIfPlanChangeRequired_False() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members

        // Act
        boolean result = billingService.checkIfPlanChangeRequired(testWorkspace.getId(), 1); // Add 1 more member

        // Assert
        assertFalse(result);
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testValidatePlanChangeForNewMembers_NoChangeRequired() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members

        // Act - should not throw exception
        billingService.validatePlanChangeForNewMembers(testWorkspace.getId(), 1); // Add 1 more member

        // Assert
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testValidatePlanChangeForNewMembers_ChangeRequired_PaymentRegistered() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(true);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            billingService.validatePlanChangeForNewMembers(testWorkspace.getId(), 3); // Add 3 more members
        });
        assertEquals("PLAN_CHANGE_REQUIRED", exception.getCode());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
    }

    @Test
    void testValidatePlanChangeForNewMembers_ChangeRequired_PaymentNotRegistered() {
        // Arrange
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 members
        when(paymentService.isPaymentRegistered(testWorkspace)).thenReturn(false);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            billingService.validatePlanChangeForNewMembers(testWorkspace.getId(), 3); // Add 3 more members
        });
        assertEquals("PAYMENT_REQUIRED", exception.getCode());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
        verify(paymentService).isPaymentRegistered(testWorkspace);
    }

    @Test
    void testCountActiveWorkspaceMembers() {
        // Arrange
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 active members

        // Act
        int result = billingService.countActiveWorkspaceMembers(testWorkspace.getId());

        // Assert
        assertEquals(3, result);
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testGetMemberCountHistory() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<WorkspaceBilling> historyList = new ArrayList<>();
        WorkspaceBilling billing = new WorkspaceBilling();
        billing.setWorkspace(testWorkspace);
        billing.setYear(previousMonth.getYear());
        billing.setMonth(previousMonth.getMonthValue());
        billing.setMemberCount(2);
        billing.setBillingPlan(BillingPlan.BASIC);
        historyList.add(billing);

        when(workspaceBillingRepository.findHistoryByWorkspaceIdOrderByYearMonthDesc(testWorkspace.getId()))
                .thenReturn(historyList);
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 active members

        // Act
        Map<String, Integer> result = billingService.getMemberCountHistory(testWorkspace.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2, result.get(previousMonth.toString()));
        assertEquals(3, result.get(currentMonth.toString()));
        verify(workspaceBillingRepository).findHistoryByWorkspaceIdOrderByYearMonthDesc(testWorkspace.getId());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }

    @Test
    void testGetBillingAmountHistory() {
        // Arrange
        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<WorkspaceBilling> historyList = new ArrayList<>();
        WorkspaceBilling billing = new WorkspaceBilling();
        billing.setWorkspace(testWorkspace);
        billing.setYear(previousMonth.getYear());
        billing.setMonth(previousMonth.getMonthValue());
        billing.setMemberCount(2);
        billing.setBillingPlan(BillingPlan.BASIC);
        billing.setAmount(0); // BASIC plan, 0 KRW per member
        historyList.add(billing);

        when(workspaceBillingRepository.findHistoryByWorkspaceIdOrderByYearMonthDesc(testWorkspace.getId()))
                .thenReturn(historyList);
        when(workspacePlanRepository.findByWorkspaceId(testWorkspace.getId()))
                .thenReturn(Optional.of(testWorkspacePlan));
        when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId()))
                .thenReturn(testWorkspaceMembers); // 3 active members

        // Act
        Map<String, Integer> result = billingService.getBillingAmountHistory(testWorkspace.getId());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(0, result.get(previousMonth.toString()));
        assertEquals(0, result.get(currentMonth.toString())); // BASIC plan, 0 KRW per member
        verify(workspaceBillingRepository).findHistoryByWorkspaceIdOrderByYearMonthDesc(testWorkspace.getId());
        verify(workspacePlanRepository).findByWorkspaceId(testWorkspace.getId());
        verify(workspaceMemberService).getWorkspaceMembersByWorkspaceId(testWorkspace.getId());
    }
}
