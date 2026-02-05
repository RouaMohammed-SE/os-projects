package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

// =====================================================
// Test Case Data Classes
// =====================================================

class TestCase {
    public String name;
    public TestInput input;
    public ExpectedOutput expectedOutput;
}

class TestInput {
    public int contextSwitch;
    public int rrQuantum;
    public int agingInterval;
    public List<ProcessJSON> processes;
}

class ExpectedOutput {
    public SchedulerOutput SJF;
    public SchedulerOutput RR;
    public SchedulerOutput Priority;
}

class SchedulerOutput {
    public List<String> executionOrder;
    public List<ProcessResult> processResults;
    public double averageWaitingTime;
    public double averageTurnaroundTime;
}

// AG Test Classes
class AGTestCase {
    public AGInput input;
    public AGExpectedOutput expectedOutput;
}

class AGExpectedOutput {
    public List<String> executionOrder;
    public List<AGProcessResult> processResults;
    public double averageWaitingTime;
    public double averageTurnaroundTime;
}

class AGProcessResult {
    public String name;
    public int waitingTime;
    public int turnaroundTime;
    public List<Integer> quantumHistory;
}

// =====================================================
// Main Test Class
// =====================================================

@DisplayName("CPU Scheduler Simulator Tests")
class SimulatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // =====================================================
    // NON-AG SCHEDULER TESTS (test_1.json to test_6.json)
    // =====================================================

    @ParameterizedTest(name = "Non-AG Test Case {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    @DisplayName("Test Non-AG Schedulers (SJF, RR, Priority)")
    void testNonAGSchedulers(int testNumber) throws Exception {
        String fileName = "test_" + testNumber + ".json";

        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        assertNotNull(is, fileName + " not found in resources");

        TestCase tc = mapper.readValue(is, TestCase.class);

        System.out.println("\n========================================");
        System.out.println("Running: " + (tc.name != null ? tc.name : fileName));
        System.out.println("========================================");

        List<Process> baseProcesses = tc.input.processes.stream()
                .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, 0))
                .collect(Collectors.toList());

        // Test SJF Scheduler
        testSJF(tc, baseProcesses);

        // Test Round Robin Scheduler
        testRoundRobin(tc, baseProcesses);

        // Test Priority Scheduler
        testPriority(tc, baseProcesses);

        System.out.println("✓ All tests passed for " + fileName);
    }

    private void testSJF(TestCase tc, List<Process> baseProcesses) {
        System.out.println("\n--- Testing SJF Scheduler ---");

        SimulationResult sjfResult = new SJFScheduler(tc.input.contextSwitch)
                .simulate(copyProcessList(baseProcesses));

        // Test execution order
        assertEquals(tc.expectedOutput.SJF.executionOrder, sjfResult.executionOrder,
                "SJF: Execution order mismatch");

        // Test average waiting time
        assertEquals(tc.expectedOutput.SJF.averageWaitingTime, sjfResult.averageWaitingTime, 0.01,
                "SJF: Average waiting time mismatch");

        // Test average turnaround time
        assertEquals(tc.expectedOutput.SJF.averageTurnaroundTime, sjfResult.averageTurnaroundTime, 0.01,
                "SJF: Average turnaround time mismatch");

        // Test individual process results
        for (ProcessResult expected : tc.expectedOutput.SJF.processResults) {
            ProcessResult actual = findProcessResult(sjfResult.processResults, expected.name);
            assertNotNull(actual, "SJF: Process " + expected.name + " not found in results");
            assertEquals(expected.waitingTime, actual.waitingTime,
                    "SJF: Waiting time mismatch for " + expected.name);
            assertEquals(expected.turnaroundTime, actual.turnaroundTime,
                    "SJF: Turnaround time mismatch for " + expected.name);
        }

        System.out.println("✓ SJF tests passed");
    }

    private void testRoundRobin(TestCase tc, List<Process> baseProcesses) {
        System.out.println("\n--- Testing Round Robin Scheduler ---");

        SimulationResult rrResult = new RoundRobinScheduler(tc.input.rrQuantum, tc.input.contextSwitch)
                .simulate(copyProcessList(baseProcesses));

        // Test execution order
        assertEquals(tc.expectedOutput.RR.executionOrder, rrResult.executionOrder,
                "RR: Execution order mismatch");

        // Test average waiting time
        assertEquals(tc.expectedOutput.RR.averageWaitingTime, rrResult.averageWaitingTime, 0.01,
                "RR: Average waiting time mismatch");

        // Test average turnaround time
        assertEquals(tc.expectedOutput.RR.averageTurnaroundTime, rrResult.averageTurnaroundTime, 0.01,
                "RR: Average turnaround time mismatch");

        // Test individual process results
        for (ProcessResult expected : tc.expectedOutput.RR.processResults) {
            ProcessResult actual = findProcessResult(rrResult.processResults, expected.name);
            assertNotNull(actual, "RR: Process " + expected.name + " not found in results");
            assertEquals(expected.waitingTime, actual.waitingTime,
                    "RR: Waiting time mismatch for " + expected.name);
            assertEquals(expected.turnaroundTime, actual.turnaroundTime,
                    "RR: Turnaround time mismatch for " + expected.name);
        }

        System.out.println("✓ Round Robin tests passed");
    }

    private void testPriority(TestCase tc, List<Process> baseProcesses) {
        System.out.println("\n--- Testing Priority Scheduler ---");

        SimulationResult prResult = new PriorityScheduler(tc.input.contextSwitch, tc.input.agingInterval)
                .simulate(copyProcessList(baseProcesses));

        // Test execution order
        assertEquals(tc.expectedOutput.Priority.executionOrder, prResult.executionOrder,
                "Priority: Execution order mismatch");

        // Test average waiting time
        assertEquals(tc.expectedOutput.Priority.averageWaitingTime, prResult.averageWaitingTime, 0.01,
                "Priority: Average waiting time mismatch");

        // Test average turnaround time
        assertEquals(tc.expectedOutput.Priority.averageTurnaroundTime, prResult.averageTurnaroundTime, 0.01,
                "Priority: Average turnaround time mismatch");

        // Test individual process results
        for (ProcessResult expected : tc.expectedOutput.Priority.processResults) {
            ProcessResult actual = findProcessResult(prResult.processResults, expected.name);
            assertNotNull(actual, "Priority: Process " + expected.name + " not found in results");
            assertEquals(expected.waitingTime, actual.waitingTime,
                    "Priority: Waiting time mismatch for " + expected.name);
            assertEquals(expected.turnaroundTime, actual.turnaroundTime,
                    "Priority: Turnaround time mismatch for " + expected.name);
        }

        System.out.println("✓ Priority tests passed");
    }

    // =====================================================
    // AG SCHEDULER TESTS (AG_test1.json to AG_test6.json)
    // =====================================================

    @ParameterizedTest(name = "AG Test Case {0}")
    @ValueSource(ints = {1, 2, 3, 4, 5, 6})
    @DisplayName("Test AG Scheduler")
    void testAGScheduler(int testNumber) throws Exception {
        String fileName = "AG_test" + testNumber + ".json";

        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        assertNotNull(is, fileName + " not found in resources");

        AGTestCase tc = mapper.readValue(is, AGTestCase.class);

        System.out.println("\n========================================");
        System.out.println("Running AG Test: " + fileName);
        System.out.println("========================================");

        List<Process> processes = tc.input.processes.stream()
                .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, p.quantum))
                .collect(Collectors.toList());

        SimulationResult result = new AGScheduler(0).simulate(processes);

        // Test execution order
        assertEquals(tc.expectedOutput.executionOrder, result.executionOrder,
                "AG: Execution order mismatch");

        // Test average waiting time
        assertEquals(tc.expectedOutput.averageWaitingTime, result.averageWaitingTime, 0.01,
                "AG: Average waiting time mismatch");

        // Test average turnaround time
        assertEquals(tc.expectedOutput.averageTurnaroundTime, result.averageTurnaroundTime, 0.01,
                "AG: Average turnaround time mismatch");

        // Test quantum history for each process
        for (AGProcessResult expected : tc.expectedOutput.processResults) {
            List<Integer> actualQuantumHistory = result.quantumHistory.get(expected.name);
            assertNotNull(actualQuantumHistory,
                    "AG: Quantum history not found for " + expected.name);
            assertEquals(expected.quantumHistory, actualQuantumHistory,
                    "AG: Quantum history mismatch for " + expected.name);

            // Test individual waiting and turnaround times
            ProcessResult actual = findProcessResult(result.processResults, expected.name);
            assertNotNull(actual, "AG: Process " + expected.name + " not found in results");
            assertEquals(expected.waitingTime, actual.waitingTime,
                    "AG: Waiting time mismatch for " + expected.name);
            assertEquals(expected.turnaroundTime, actual.turnaroundTime,
                    "AG: Turnaround time mismatch for " + expected.name);
        }

        System.out.println("✓ AG Scheduler tests passed for " + fileName);
    }

    // =====================================================
    // INDIVIDUAL TEST METHODS (for specific test cases)
    // =====================================================

    @Test
    @DisplayName("Test Non-AG Case 1: Basic mixed arrivals")
    void testNonAG_case1() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test_1.json");
        assertNotNull(is, "test_1.json not found");

        TestCase tc = mapper.readValue(is, TestCase.class);
        List<Process> baseProcesses = tc.input.processes.stream()
                .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, 0))
                .collect(Collectors.toList());

        // SJF Test
        SimulationResult sjf = new SJFScheduler(tc.input.contextSwitch)
                .simulate(copyProcessList(baseProcesses));
        assertEquals(tc.expectedOutput.SJF.executionOrder, sjf.executionOrder);
        assertEquals(tc.expectedOutput.SJF.averageWaitingTime, sjf.averageWaitingTime, 0.01);

        // RR Test
        SimulationResult rr = new RoundRobinScheduler(tc.input.rrQuantum, tc.input.contextSwitch)
                .simulate(copyProcessList(baseProcesses));
        assertEquals(tc.expectedOutput.RR.executionOrder, rr.executionOrder);

        // Priority Test
        SimulationResult pr = new PriorityScheduler(tc.input.contextSwitch, tc.input.agingInterval)
                .simulate(copyProcessList(baseProcesses));
        assertEquals(tc.expectedOutput.Priority.executionOrder, pr.executionOrder);
    }

    @Test
    @DisplayName("Test AG Case 1")
    void testAG_case1() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("AG_test1.json");
        assertNotNull(is, "AG_test1.json not found");

        AGTestCase tc = mapper.readValue(is, AGTestCase.class);
        List<Process> processes = tc.input.processes.stream()
                .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, p.quantum))
                .collect(Collectors.toList());

        SimulationResult result = new AGScheduler(0).simulate(processes);

        assertEquals(tc.expectedOutput.executionOrder, result.executionOrder);

        tc.expectedOutput.processResults.forEach(ep ->
                assertEquals(ep.quantumHistory, result.quantumHistory.get(ep.name))
        );
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private List<Process> copyProcessList(List<Process> list) {
        return list.stream()
                .map(p -> new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum))
                .collect(Collectors.toList());
    }

    private ProcessResult findProcessResult(List<ProcessResult> results, String name) {
        return results.stream()
                .filter(pr -> pr.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}

// =====================================================
// Test Runner with Summary
// =====================================================

class TestRunner {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("CPU SCHEDULER SIMULATOR - JUNIT TEST SUITE");
        System.out.println("=".repeat(80));
        System.out.println("\nTests will validate:");
        System.out.println("  • 6 Non-AG test cases (SJF, Round Robin, Priority)");
        System.out.println("  • 6 AG test cases (AG Scheduler with quantum history)");
        System.out.println("\nRun with: mvn test or your IDE's test runner");
        System.out.println("=".repeat(80));
    }
}