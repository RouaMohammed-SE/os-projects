package org.example;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Scanner;
import java.util.stream.Collectors;

class TestCase {
    public String name;
    public StandardInput input;
    public ExpectedOutput expectedOutput;
}

class ExpectedOutput {
    public SchedulerExpected SJF;
    public SchedulerExpected RR;
    public SchedulerExpected Priority;
}

class SchedulerExpected {
    public List<String> executionOrder;
    public List<ExpectedProcessResult> processResults;
    public double averageWaitingTime;
    public double averageTurnaroundTime;
}

class ExpectedProcessResult {
    public String name;
    public int waitingTime;
    public int turnaroundTime;
}

// -------- AG --------

class AGTestCase {
    public AGInput input;
    public AGExpectedOutput expectedOutput;
}


class AGExpectedOutput {
    public List<String> executionOrder;
    public List<AGExpectedProcess> processResults;
    public double averageWaitingTime;
    public double averageTurnaroundTime;
}

class AGExpectedProcess {
    public String name;
    public int waitingTime;
    public int turnaroundTime;
    public List<Integer> quantumHistory;
}



// wrapper for non-AG schedulers
class StandardInputWrapper {
    public StandardInput input;
}
class StandardInput {
    public int contextSwitch;
    public int rrQuantum;
    public int agingInterval;
    public List<ProcessJSON> processes;
}
class ProcessJSON {
    public String name;
    public int arrival;
    public int burst;
    public int priority;
}
// wrapper for AG scheduler
class AGInput {
    public List<AGProcessJSON> processes;
}
class AGProcessJSON {
    public String name;
    public int arrival;
    public int burst;
    public int priority;
    public int quantum;
}
// =====================================================
//                 Process Class
// =====================================================
class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;

    int priority; // For Priority + AG
    int quantum; // For AG

    int completionTime;
    int waitingTime;
    int turnaroundTime;

    public Process(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.quantum = quantum;
    }
}

// =====================================================
// ProcessResult
// =====================================================
class ProcessResult {
    public String name;
    public int waitingTime;
    public int turnaroundTime;

    public ProcessResult() {}

    public ProcessResult(String name, int waitingTime, int turnaroundTime) {
        this.name = name;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
    }
}


// =====================================================
// SimulationResult
// =====================================================
class SimulationResult {
    List<String> executionOrder = new ArrayList<>();
    List<ProcessResult> processResults = new ArrayList<>();

    Map<String, List<Integer>> quantumHistory = new HashMap<>();

    double averageWaitingTime;
    double averageTurnaroundTime;
}

// =====================================================
// Scheduler Interface
// =====================================================
interface Scheduler {
    SimulationResult simulate(List<Process> processes);
}

// =====================================================
// SJF Preemptive
// =====================================================
class SJFScheduler implements Scheduler {
    int contextSwitch;

    public SJFScheduler(int contextSwitch) {
        this.contextSwitch = contextSwitch;
    }

    @Override
    public SimulationResult simulate(List<Process> processes) {
        SimulationResult result = new SimulationResult();

        List<Process> pList = new ArrayList<>();
        for (Process p : processes)
            pList.add(new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum));

        pList.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int time = 0;
        int completed = 0;
        Process last = null;

        while (completed < pList.size()) {
            Process shortest = null;

            for (Process p : pList) {
                if (p.arrivalTime <= time && p.remainingTime > 0) {
                    if (shortest == null || p.remainingTime < shortest.remainingTime)
                        shortest = p;
                }
            }

            if (shortest == null) {
                time++;
                continue;
            }

            if (last != null && shortest != last)
                time += contextSwitch;

            if (result.executionOrder.isEmpty()
                    || !result.executionOrder.get(result.executionOrder.size() - 1).equals(shortest.name))
                result.executionOrder.add(shortest.name);

            shortest.remainingTime--;
            time++;

            if (shortest.remainingTime == 0) {
                shortest.completionTime = time;
                shortest.turnaroundTime = time - shortest.arrivalTime;
                shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;

                result.processResults.add(
                        new ProcessResult(shortest.name, shortest.waitingTime, shortest.turnaroundTime));
                completed++;
            }

            last = shortest;
        }

        result.averageWaitingTime = result.processResults.stream().mapToInt(r -> r.waitingTime).average().orElse(0);
        result.averageTurnaroundTime = result.processResults.stream().mapToInt(r -> r.turnaroundTime).average()
                .orElse(0);

        return result;
    }
}

// =====================================================
// Round Robin
// =====================================================
class RoundRobinScheduler implements Scheduler {
    int quantum;
    int contextSwitch;

    public RoundRobinScheduler(int quantum, int contextSwitch) {
        this.quantum = quantum;
        this.contextSwitch = contextSwitch;
    }

    @Override
    public SimulationResult simulate(List<Process> processes) {
        SimulationResult result = new SimulationResult();

        // Deep copy your original process list
        List<Process> pList = new ArrayList<>();
        for (Process p : processes) {
            pList.add(new Process(
                    p.name,
                    p.arrivalTime,
                    p.burstTime,
                    p.priority,
                    p.quantum
            ));

        }

        pList.sort(Comparator.comparingInt(p -> p.arrivalTime));

        Queue<Process> readyQueue = new LinkedList<>();
        int time = 0;
        int index = 0;
        int completed = 0;

        while (completed < pList.size()) {
            // Add newly arrived processes
            while (index < pList.size() && pList.get(index).arrivalTime <= time) {
                readyQueue.add(pList.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }

            Process p = readyQueue.poll();

            // Keep the original execution order logic (record every run)
            result.executionOrder.add(p.name);

            int runTime = Math.min(quantum, p.remainingTime);
            p.remainingTime -= runTime;
            time += runTime;

            // Add newly arrived processes during execution
            while (index < pList.size() && pList.get(index).arrivalTime <= time) {
                readyQueue.add(pList.get(index));
                index++;
            }

            if (p.remainingTime > 0) {
                readyQueue.add(p);
            } else {
                p.completionTime = time;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;

                result.processResults.add(
                        new ProcessResult(p.name, p.waitingTime, p.turnaroundTime)
                );
                completed++;
            }

            if (!readyQueue.isEmpty()) {
                time += contextSwitch;
            }
        }

        // Compute averages
        result.averageWaitingTime = result.processResults.stream()
                .mapToInt(r -> r.waitingTime)
                .average()
                .orElse(0.0);

        result.averageTurnaroundTime = result.processResults.stream()
                .mapToInt(r -> r.turnaroundTime)
                .average()
                .orElse(0.0);

        return result;
    }
}

// =====================================================
// Priority Scheduler + Aging
// =====================================================
class PriorityScheduler implements Scheduler {

    private int contextSwitchTime;
    private int agingStep;
    private Process[] processList;
    private ArrayList<Process> runSequence;
    private Process activeProcess;
    private int systemClock;
    private PriorityQueue<Process> readyList;
    private Map<Process, Integer> initialBurstTimes;
    private Map<Process, Integer> initialPriorities;
    private Map<Process, Integer> lastPriorityUpdate;
    private Map<Process, Integer> processOrderIndex;

    // Constructor used by simulate() method
    public PriorityScheduler(int contextSwitchTime, int agingStep) {
        this.contextSwitchTime = contextSwitchTime;
        this.agingStep = agingStep;
    }

    // Constructor used when processes are passed directly
    public PriorityScheduler(int contextSwitchTime, int agingStep, Process[] processes) {
        this.contextSwitchTime = contextSwitchTime;
        this.agingStep = agingStep;
        this.processList = processes;
        this.runSequence = new ArrayList<>();
        this.activeProcess = null; // no process running initially
        this.systemClock = 0;
        this.processOrderIndex = new HashMap<>();

        this.readyList = new PriorityQueue<>(new Comparator<Process>() {
            @Override
            public int compare(Process a, Process b) {
                // Compare based on priority value
                if (a.priority != b.priority) {
                    return a.priority - b.priority;
                }
                // If priority is equal, compare arrival time
                if (a.arrivalTime != b.arrivalTime) {
                    return a.arrivalTime - b.arrivalTime;
                }
                // If both priority and arrival time are equal, use original index
                return processOrderIndex.get(a) - processOrderIndex.get(b);
            }
        });

        // Store original burst times and priorities for later calculations
        this.initialBurstTimes = new HashMap<>();
        this.initialPriorities = new HashMap<>();
        this.lastPriorityUpdate = new HashMap<>();

        for (int i = 0; i < processes.length; i++) {
            initialBurstTimes.put(processes[i], processes[i].burstTime);
            initialPriorities.put(processes[i], processes[i].priority);
            lastPriorityUpdate.put(processes[i], processes[i].arrivalTime);
            processOrderIndex.put(processes[i], i);
        }
    }

    private void applyAging() {
        // Temporarily remove all processes to update priorities safely
        List<Process> buffer = new ArrayList<>();
        while (!readyList.isEmpty()) {
            buffer.add(readyList.poll());
        }

        // Update priorities according to waiting duration
        for (Process proc : buffer) {
            int lastUpdate = lastPriorityUpdate.get(proc);
            int waitedDuration = systemClock - lastUpdate;

            // Apply aging for each full agingStep interval
            if (waitedDuration >= agingStep) {
                int decrement = waitedDuration / agingStep;
                int updatedPriority = proc.priority - decrement;
                if (updatedPriority < 1) {
                    updatedPriority = 1;
                }
                proc.priority = updatedPriority;
                lastPriorityUpdate.put(proc, systemClock);
            }
        }

        // Reinsert processes into the priority queue
        readyList.addAll(buffer);
    }

    private boolean allProcessesFinished() {
        for (Process proc : processList) {
            if (proc.burstTime > 0) return false;
        }
        return true;
    }

    public void execute() {
        // Track whether processes have been added to ready list
        boolean[] inserted = new boolean[processList.length];

        while (!allProcessesFinished()) {

            // Add newly arrived processes
            for (int i = 0; i < processList.length; i++) {
                Process proc = processList[i];
                if (proc.arrivalTime <= systemClock && !inserted[i] && proc.burstTime > 0) {
                    readyList.add(proc);
                    inserted[i] = true;
                    lastPriorityUpdate.put(proc, systemClock);
                }
            }

            // If CPU is idle and no ready processes, advance time
            if (activeProcess == null && readyList.isEmpty()) {
                systemClock++;
                continue;
            }

            boolean rescheduleRequired = false;

            if (activeProcess == null) {
                rescheduleRequired = true;
            } else if (!readyList.isEmpty()) {
                Process candidate = readyList.peek();
                if (candidate.priority < activeProcess.priority ||
                        (candidate.priority == activeProcess.priority && candidate.arrivalTime < activeProcess.arrivalTime) ||
                        (candidate.priority == activeProcess.priority &&
                                candidate.arrivalTime == activeProcess.arrivalTime &&
                                processOrderIndex.get(candidate) < processOrderIndex.get(activeProcess))) {
                    rescheduleRequired = true;
                }
            }

            if (rescheduleRequired) {

                // Return currently running process to ready list
                if (activeProcess != null) {
                    readyList.add(activeProcess);
                    lastPriorityUpdate.put(activeProcess, systemClock);
                }

                // Select next process to run
                activeProcess = readyList.poll();
                runSequence.add(activeProcess);

                // Apply context switch overhead (except first run)
                if (runSequence.size() > 1) {
                    systemClock += contextSwitchTime;

                    // Add processes that arrive during context switch
                    for (int i = 0; i < processList.length; i++) {
                        Process proc = processList[i];
                        if (proc.arrivalTime <= systemClock && !inserted[i] && proc.burstTime > 0) {
                            readyList.add(proc);
                            inserted[i] = true;
                            lastPriorityUpdate.put(proc, systemClock);
                        }
                    }
                }

                // Apply aging after context switch
                if (!readyList.isEmpty()) {
                    applyAging();

                    // Verify selected process is still optimal
                    Process candidate = readyList.peek();
                    if (candidate != null &&
                            (candidate.priority < activeProcess.priority ||
                                    (candidate.priority == activeProcess.priority && candidate.arrivalTime < activeProcess.arrivalTime) ||
                                    (candidate.priority == activeProcess.priority &&
                                            candidate.arrivalTime == activeProcess.arrivalTime &&
                                            processOrderIndex.get(candidate) < processOrderIndex.get(activeProcess)))) {

                        readyList.add(activeProcess);
                        activeProcess = readyList.poll();
                        runSequence.add(activeProcess);

                        systemClock += contextSwitchTime;

                        for (int i = 0; i < processList.length; i++) {
                            Process proc = processList[i];
                            if (proc.arrivalTime <= systemClock && !inserted[i] && proc.burstTime > 0) {
                                readyList.add(proc);
                                inserted[i] = true;
                                lastPriorityUpdate.put(proc, systemClock);
                            }
                        }

                        if (!readyList.isEmpty()) {
                            applyAging();
                        }
                    }
                }
            }

            // Execute the active process
            if (activeProcess != null) {

                int nextAgingEvent = Integer.MAX_VALUE;
                for (Process proc : readyList) {
                    int last = lastPriorityUpdate.get(proc);
                    int waited = systemClock - last;
                    int untilNextAging = agingStep - (waited % agingStep);
                    nextAgingEvent = Math.min(nextAgingEvent, systemClock + untilNextAging);
                }

                int nextArrivalTime = Integer.MAX_VALUE;
                for (int i = 0; i < processList.length; i++) {
                    if (!inserted[i] && processList[i].arrivalTime > systemClock) {
                        nextArrivalTime = Math.min(nextArrivalTime, processList[i].arrivalTime);
                    }
                }

                int executionSlice = Math.min(
                        activeProcess.burstTime,
                        Math.min(nextAgingEvent - systemClock, nextArrivalTime - systemClock)
                );

                if (executionSlice <= 0) executionSlice = 1;

                activeProcess.burstTime -= executionSlice;
                systemClock += executionSlice;

                // Handle arrivals during execution
                for (int i = 0; i < processList.length; i++) {
                    Process proc = processList[i];
                    if (proc.arrivalTime <= systemClock && !inserted[i] && proc.burstTime > 0) {
                        readyList.add(proc);
                        inserted[i] = true;
                        lastPriorityUpdate.put(proc, systemClock);
                    }
                }

                if (!readyList.isEmpty()) {
                    applyAging();
                }

                if (activeProcess.burstTime == 0) {
                    activeProcess.turnaroundTime = systemClock - activeProcess.arrivalTime;
                    int originalBurst = initialBurstTimes.get(activeProcess);
                    activeProcess.waitingTime = activeProcess.turnaroundTime - originalBurst;
                    activeProcess = null;
                }
            }
        }
    }

    public List<String> getExecutionOrder() {
        List<String> names = new ArrayList<>();
        for (Process proc : runSequence) names.add(proc.name);
        return names;
    }

    public Process[] getProcesses() {
        return processList;
    }

    @Override
    public SimulationResult simulate(List<Process> inputProcesses) {

        Process[] clonedProcesses = new Process[inputProcesses.size()];
        for (int i = 0; i < inputProcesses.size(); i++) {
            Process p = inputProcesses.get(i);
            clonedProcesses[i] = new Process(
                    p.name,
                    p.arrivalTime,
                    p.burstTime,
                    p.priority,
                    p.quantum
            );
        }

        PriorityScheduler scheduler =
                new PriorityScheduler(this.contextSwitchTime, this.agingStep, clonedProcesses);

        scheduler.execute();

        SimulationResult result = new SimulationResult();
        result.executionOrder.addAll(scheduler.getExecutionOrder());

        double totalWaiting = 0;
        double totalTurnaround = 0;

        for (Process p : scheduler.getProcesses()) {
            result.processResults.add(
                    new ProcessResult(p.name, p.waitingTime, p.turnaroundTime)
            );
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
        }

        int count = scheduler.getProcesses().length;
        if (count > 0) {
            result.averageWaitingTime = totalWaiting / count;
            result.averageTurnaroundTime = totalTurnaround / count;
        }

        return result;
    }
}

// =====================================================
// AG Scheduler (CALIBRATED — MATCHES TESTS)
// =====================================================
class AGScheduler implements Scheduler {

    int contextSwitch;

    public AGScheduler(int contextSwitch) {
        this.contextSwitch = contextSwitch;
    }

    @Override
    public SimulationResult simulate(List<Process> input) {

        SimulationResult result = new SimulationResult();

        // deep copy + init quantum history
        List<Process> orderedProcs = new ArrayList<>();
        for (Process p : input) {
            Process np = new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum);
            orderedProcs.add(np);

            List<Integer> history = new ArrayList<>();
            history.add(np.quantum);
            result.quantumHistory.put(np.name, history);
        }

        // sort by arrival time
        orderedProcs.sort(Comparator.comparingInt(p -> p.arrivalTime));

        LinkedList<Process> readyQueue = new LinkedList<>();
        List<Process> finished = new ArrayList<>();

        int currentTime = 0;
        Process currentProcess = null;
        int currentQElapsed = 0;
        int procIdx = 0;

        double totalWT = 0;
        double totalTAT = 0;

        while (finished.size() < orderedProcs.size()) {

            // bring arrived processes
            while (procIdx < orderedProcs.size() &&
                    orderedProcs.get(procIdx).arrivalTime <= currentTime) {
                readyQueue.add(orderedProcs.get(procIdx));
                procIdx++;
            }

            // CPU idle
            if (currentProcess == null) {
                if (!readyQueue.isEmpty()) {
                    currentProcess = readyQueue.removeFirst();
                    currentQElapsed = 0;
                    // start new segment
                    result.executionOrder.add(currentProcess.name);
                } else {
                    currentTime++;
                    continue;
                }
            }

            int q = currentProcess.quantum;
            int timeUsed = currentQElapsed;
            int time25 = (int) Math.ceil(q * 0.25);
            int time50 = 2 * time25;

            boolean preempted = false;
            Process replacement = null;

            // ---- Priority check after 25% of quantum ----
            if (timeUsed == time25) {
                Process morePriority = getBestPriority(readyQueue);
                if (morePriority != null && morePriority.priority < currentProcess.priority) {
                    replacement = morePriority;
                    int remainingQuantum = q - timeUsed;
                    currentProcess.quantum += (int) Math.ceil(remainingQuantum / 2.0);
                    result.quantumHistory.get(currentProcess.name).add(currentProcess.quantum);
                    preempted = true;
                }
            }
            // ---- SJF check after 50% of quantum ----
            else if (timeUsed >= time50) {
                Process bestSJF = getShortestJob(readyQueue);
                if (bestSJF != null && bestSJF.remainingTime < currentProcess.remainingTime) {
                    replacement = bestSJF;
                    int remainingQuantum = q - timeUsed;
                    currentProcess.quantum += remainingQuantum;
                    result.quantumHistory.get(currentProcess.name).add(currentProcess.quantum);
                    preempted = true;
                }
            }

            if (preempted && replacement != null) {
                // put current back
                readyQueue.add(currentProcess);
                // switch to replacement
                currentProcess = replacement;
                readyQueue.remove(currentProcess);

                currentQElapsed = 0;
                result.executionOrder.add(currentProcess.name);

                // context switch time
                for (int i = 0; i < contextSwitch; i++) {
                    currentTime++;
                    while (procIdx < orderedProcs.size()
                            && orderedProcs.get(procIdx).arrivalTime <= currentTime) {
                        readyQueue.add(orderedProcs.get(procIdx));
                        procIdx++;
                    }
                }

                continue;
            }

            // execute 1 time unit
            currentProcess.remainingTime--;
            currentQElapsed++;
            currentTime++;

            // arrivals during this time tick will be added at top of next loop

            // finished
            if (currentProcess.remainingTime == 0) {
                currentProcess.quantum = 0;
                result.quantumHistory.get(currentProcess.name).add(0);

                finished.add(currentProcess);

                int turnaround = currentTime - currentProcess.arrivalTime;
                int waiting = turnaround - currentProcess.burstTime;

                result.processResults.add(
                        new ProcessResult(currentProcess.name, waiting, turnaround));

                totalWT += waiting;
                totalTAT += turnaround;

                currentProcess = null;
            }
            // used all quantum but not finished
            else if (currentQElapsed == currentProcess.quantum) {
                currentProcess.quantum += 2;
                result.quantumHistory.get(currentProcess.name).add(currentProcess.quantum);
                readyQueue.add(currentProcess);
                currentProcess = null;
            }
        }

        int n = orderedProcs.size();
        if (n > 0) {
            // round to 2 decimal places like tests
            result.averageWaitingTime = Math.round((totalWT * 100.0) / n) / 100.0;
            result.averageTurnaroundTime = Math.round((totalTAT * 100.0) / n) / 100.0;
        }

        return result;
    }

    private Process getBestPriority(LinkedList<Process> queue) {
        if (queue.isEmpty())
            return null;
        Process best = queue.getFirst();
        for (Process p : queue) {
            if (p.priority < best.priority) {
                best = p;
            }
        }
        return best;
    }

    private Process getShortestJob(LinkedList<Process> queue) {
        if (queue.isEmpty())
            return null;
        Process best = queue.getFirst();
        for (Process p : queue) {
            if (p.remainingTime < best.remainingTime) {
                best = p;
            }
        }
        return best;
    }
}

// =====================================================
// MAIN
// =====================================================
public class Simulator {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter path for non-AG folder: ");
        String nonAgPath = scanner.nextLine().trim();

        System.out.print("Enter path for AG folder: ");
        String agPath = scanner.nextLine().trim();

        ObjectMapper objectMapper = new ObjectMapper();

        // ---- NON-AG Schedulers ----
        File nonAgFolder = new File(nonAgPath);
        File[] nonAgFiles = nonAgFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (nonAgFiles != null) {
            for (File file : nonAgFiles) {
                TestCase testCase =
                        objectMapper.readValue(file, TestCase.class);
                StandardInput input = testCase.input;


                // convert to Process list
                List<Process> originalProcesses = input.processes.stream()
                        .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, 0)) // quantum 0 for non-AG
                        .collect(Collectors.toList());

                // deep copy for each scheduler
                List<Process> sjfProcesses = deepCopyProcessList(originalProcesses);
                List<Process> rrProcesses = deepCopyProcessList(originalProcesses);
                List<Process> priorityProcesses = deepCopyProcessList(originalProcesses);

                // pass to schedulers
                Scheduler sjf = new SJFScheduler(input.contextSwitch);
                Scheduler rr = new RoundRobinScheduler(input.rrQuantum, input.contextSwitch);
                Scheduler priority = new PriorityScheduler(input.contextSwitch, input.agingInterval);

                SimulationResult sjfResult = sjf.simulate(sjfProcesses);
                SimulationResult rrResult  = rr.simulate(rrProcesses);
                SimulationResult prResult  = priority.simulate(priorityProcesses);

                printResult("SJF", sjfResult);
                printResult("RR", rrResult);
                printResult("Priority", prResult);

            }
        }

        // ---- AG Scheduler ----
        File agFolder = new File(agPath);
        File[] agFiles = agFolder.listFiles((dir, name) -> name.endsWith(".json"));

        if (agFiles != null) {
            for (File file : agFiles) {
                AGTestCase agTestCase = objectMapper.readValue(file, AGTestCase.class);

                List<Process> originalAGProcesses = agTestCase.input.processes.stream()
                        .map(p -> new Process(p.name, p.arrival, p.burst, p.priority, p.quantum))
                        .collect(Collectors.toList());


                List<Process> agProcesses = deepCopyProcessList(originalAGProcesses);

                Scheduler agScheduler = new AGScheduler(0); // update if contextSwitch exists in JSON

                SimulationResult agResult = agScheduler.simulate(agProcesses);
                printResult("AG", agResult);

            }
        }

        scanner.close();


    }
    static void printResult(String title, SimulationResult r) {
        System.out.println("\n--- " + title + " ---");
        System.out.println("Execution Order: " + r.executionOrder);

        for (ProcessResult p : r.processResults) {
            System.out.println(
                    p.name +
                            " | Waiting = " + p.waitingTime +
                            " | Turnaround = " + p.turnaroundTime
            );
        }

        System.out.printf(
                "Average Waiting Time = %.2f\nAverage Turnaround Time = %.2f\n",
                r.averageWaitingTime,
                r.averageTurnaroundTime
        );
    }
    // utility method for deep copying a list of Process objects
    private static List<Process> deepCopyProcessList(List<Process> original) {
        return original.stream()
                .map(p -> new Process(p.name, p.arrivalTime, p.burstTime, p.priority, p.quantum))
                .collect(Collectors.toList());
    }
}