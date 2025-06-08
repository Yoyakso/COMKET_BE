package com.yoyakso.comket.billing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.yoyakso.comket.billing.entity.WorkspaceMemberHistory;
import com.yoyakso.comket.billing.repository.WorkspaceMemberHistoryRepository;
import com.yoyakso.comket.billing.service.WorkspaceMemberHistoryService;
import com.yoyakso.comket.workspace.entity.Workspace;
import com.yoyakso.comket.workspace.enums.WorkspaceState;
import com.yoyakso.comket.workspace.repository.WorkspaceRepository;
import com.yoyakso.comket.workspaceMember.entity.WorkspaceMember;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;
import com.yoyakso.comket.workspaceMember.service.WorkspaceMemberService;

class WorkspaceMemberHistoryServiceTest {

	@Mock
	private WorkspaceMemberHistoryRepository workspaceMemberHistoryRepository;

	@Mock
	private WorkspaceRepository workspaceRepository;

	@Mock
	private WorkspaceMemberService workspaceMemberService;

	@InjectMocks
	private WorkspaceMemberHistoryService workspaceMemberHistoryService;

	private Workspace testWorkspace;
	private List<WorkspaceMember> testWorkspaceMembers;
	private WorkspaceMemberHistory testHistory;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Create test workspace
		testWorkspace = new Workspace();
		testWorkspace.setId(1L);
		testWorkspace.setName("Test Workspace");
		testWorkspace.setState(WorkspaceState.ACTIVE);

		// Create test workspace members
		WorkspaceMember activeMember1 = new WorkspaceMember();
		activeMember1.setId(1L);
		activeMember1.setWorkspace(testWorkspace);
		activeMember1.setState(WorkspaceMemberState.ACTIVE);

		WorkspaceMember activeMember2 = new WorkspaceMember();
		activeMember2.setId(2L);
		activeMember2.setWorkspace(testWorkspace);
		activeMember2.setState(WorkspaceMemberState.ACTIVE);

		WorkspaceMember inactiveMember = new WorkspaceMember();
		inactiveMember.setId(3L);
		inactiveMember.setWorkspace(testWorkspace);
		inactiveMember.setState(WorkspaceMemberState.INACTIVE);

		testWorkspaceMembers = Arrays.asList(activeMember1, activeMember2, inactiveMember);

		// Create test history
		YearMonth currentMonth = YearMonth.now();
		testHistory = new WorkspaceMemberHistory();
		testHistory.setId(1L);
		testHistory.setWorkspace(testWorkspace);
		testHistory.setYear(currentMonth.getYear());
		testHistory.setMonth(currentMonth.getMonthValue());
		testHistory.setMemberCount(2); // 2 active members
	}

	@Test
	void testRecordWorkspaceMemberCount() {
		// Arrange
		when(workspaceRepository.findById(testWorkspace.getId())).thenReturn(Optional.of(testWorkspace));
		when(workspaceMemberService.getWorkspaceMembersByWorkspaceId(testWorkspace.getId())).thenReturn(
			testWorkspaceMembers);
		when(workspaceMemberHistoryRepository.save(any(WorkspaceMemberHistory.class))).thenReturn(testHistory);

		// Act
		WorkspaceMemberHistory result = workspaceMemberHistoryService.recordWorkspaceMemberCount(testWorkspace.getId());

		// Assert
		assertNotNull(result);
		assertEquals(testWorkspace, result.getWorkspace());
		assertEquals(2, result.getMemberCount()); // 2 active members
		assertEquals(YearMonth.now().getYear(), result.getYear());
		assertEquals(YearMonth.now().getMonthValue(), result.getMonth());
	}

	@Test
	void testGetMemberCountHistoryMap() {
		// Arrange
		YearMonth currentMonth = YearMonth.now();
		WorkspaceMemberHistory history1 = new WorkspaceMemberHistory();
		history1.setId(1L);
		history1.setWorkspace(testWorkspace);
		history1.setYear(currentMonth.getYear());
		history1.setMonth(currentMonth.getMonthValue());
		history1.setMemberCount(2);

		WorkspaceMemberHistory history2 = new WorkspaceMemberHistory();
		history2.setId(2L);
		history2.setWorkspace(testWorkspace);
		history2.setYear(currentMonth.minusMonths(1).getYear());
		history2.setMonth(currentMonth.minusMonths(1).getMonthValue());
		history2.setMemberCount(1);

		List<WorkspaceMemberHistory> historyList = Arrays.asList(history1, history2);

		when(workspaceMemberHistoryRepository.findByWorkspaceIdOrderByYearMonthDesc(testWorkspace.getId())).thenReturn(
			historyList);

		// Act
		Map<String, Integer> result = workspaceMemberHistoryService.getMemberCountHistoryMap(testWorkspace.getId());

		// Assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(2, result.get(currentMonth.toString()));
		assertEquals(1, result.get(currentMonth.minusMonths(1).toString()));
	}
}