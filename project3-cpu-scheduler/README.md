# OS Project 3: CPU Scheduler Simulator

A comprehensive Java-based CPU scheduling simulator implementing four advanced scheduling algorithms with context switching, priority aging, and dynamic quantum management. Built with Maven and fully unit-tested with JUnit 5.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Scheduling Algorithms](#scheduling-algorithms)
- [System Architecture](#system-architecture)
- [Installation & Usage](#installation--usage)
- [Test Cases](#test-cases)
- [Performance Metrics](#performance-metrics)
- [AG Scheduler Deep Dive](#ag-scheduler-deep-dive)

## 🎯 Overview

This project implements a CPU scheduling simulator that demonstrates how different scheduling algorithms affect process execution, waiting times, and overall system performance. The simulator supports both manual input and automated testing via JSON test files.

**Course**: Operating Systems 1  
**Assignment**: Assignment 3 - CPU Schedulers Simulator  
**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Build Tool**: Maven  
**Testing Framework**: JUnit 5  
**Language**: Java 17+

### Problem Statement

Implement and compare four CPU scheduling algorithms:
1. **Preemptive Shortest Job First (SJF)** with context switching
2. **Round Robin (RR)** with configurable time quantum
3. **Priority Scheduling** with aging to prevent starvation
4. **AG Scheduling** - A novel hybrid scheduler with dynamic quantum management

## ✨ Features

### Core Features
- ✅ **4 Scheduling Algorithms**: SJF, Round Robin, Priority, AG
- ✅ **Context Switching**: Realistic overhead simulation
- ✅ **Priority Aging**: Prevents process starvation
- ✅ **Dynamic Quantum Management**: AG scheduler adapts quantum times
- ✅ **Performance Metrics**: Waiting time, turnaround time, averages
- ✅ **Execution Order Tracking**: Complete process timeline
- ✅ **Quantum History**: Tracks quantum changes (AG scheduler)

### Technical Features
- ✅ **Maven Build System**: Professional project structure
- ✅ **JUnit 5 Testing**: Comprehensive unit tests
- ✅ **JSON Test Cases**: 12+ automated test scenarios
- ✅ **Jackson JSON Parser**: Efficient data handling
- ✅ **Parameterized Tests**: Test all cases automatically
- ✅ **Clean Architecture**: Interface-based design

### Testing Features
- ✅ **6 Non-AG Test Cases**: Testing SJF, RR, Priority
- ✅ **6 AG Test Cases**: Testing AG scheduler with quantum history
- ✅ **Automated Validation**: Expected vs actual results
- ✅ **Individual Test Methods**: Targeted debugging
- ✅ **Comprehensive Assertions**: All metrics validated

## 🔄 Scheduling Algorithms

### 1. Preemptive Shortest Job First (SJF)

**Algorithm**: Always execute the process with the shortest remaining time.

**Key Features**:
- Preemptive (can interrupt running process)
- Minimizes average waiting time (optimal)
- Context switching overhead included
- Favors short processes

**Implementation Highlights**:
```java
// Select process with minimum remaining time
Process shortest = null;
for (Process p : processes) {
    if (p.arrivalTime <= time && p.remainingTime > 0) {
        if (shortest == null || p.remainingTime < shortest.remainingTime)
            shortest = p;
    }
}
```

**Use Case**: Batch systems where execution time is known

---

### 2. Round Robin (RR)

**Algorithm**: Each process gets a fixed time quantum in circular order.

**Key Features**:
- Time-sharing (fair distribution)
- Configurable quantum time
- FIFO queue management
- Context switching between quantum switches
- Prevents starvation (all processes eventually run)

**Implementation Highlights**:
```java
Queue<Process> readyQueue = new LinkedList<>();
Process p = readyQueue.poll();
int runTime = Math.min(quantum, p.remainingTime);
p.remainingTime -= runTime;
time += runTime;

if (p.remainingTime > 0) {
    readyQueue.add(p);  // Back to queue
}
```

**Parameters**:
- `quantum`: Time slice for each process (typically 2-10 time units)
- `contextSwitch`: Overhead when switching processes

**Use Case**: Time-sharing systems, interactive applications

---

### 3. Priority Scheduling with Aging

**Algorithm**: Execute highest priority process, with aging to prevent starvation.

**Key Features**:
- Preemptive based on priority
- **Priority Aging**: Increases priority of waiting processes
- Prevents indefinite blocking (starvation)
- Context switching when higher priority arrives
- Configurable aging interval

**Priority Aging Mechanism**:
```java
// Every 'agingInterval' time units, decrease priority value (increase importance)
int waitedDuration = currentTime - lastPriorityUpdate;
if (waitedDuration >= agingInterval) {
    int decrement = waitedDuration / agingInterval;
    process.priority = Math.max(1, process.priority - decrement);
}
```

**Parameters**:
- `priority`: Lower value = higher priority (1 is highest)
- `agingInterval`: Time units before priority increases
- `contextSwitch`: Switching overhead

**Starvation Prevention**: A low-priority process waiting long enough will eventually become high priority.

**Use Case**: Systems with mixed critical/non-critical tasks

---

### 4. AG Scheduling (Advanced Hybrid)

**Algorithm**: Combines FCFS, Priority, and SJF with dynamic quantum management.

**Novel Features**:
- **3-Phase Execution**: FCFS → Non-Preemptive Priority → Preemptive SJF
- **Dynamic Quantum**: Adjusts based on execution behavior
- **Quantum History Tracking**: Records all quantum changes
- **Context-Aware Scheduling**: Different rules at different quantum phases

#### AG Execution Phases

Each process executes in three phases based on quantum percentage:

**Phase 1: FCFS (0% - 25% of quantum)**
- First-Come-First-Served execution
- No preemption
- Process runs uninterrupted

**Phase 2: Non-Preemptive Priority (25% - 50% of quantum)**
- Can be preempted by higher priority process
- If preempted: `quantum += ceil(remainingQuantum / 2)`
- Priority comparison

**Phase 3: Preemptive SJF (50% - 100% of quantum)**
- Can be preempted by process with shorter remaining time
- If preempted: `quantum += remainingQuantum`
- Shortest job first comparison

#### Quantum Update Rules

The AG scheduler modifies quantum based on four scenarios:

**Scenario 1: Used All Quantum (Process Still Active)**
```
Condition: Process used entire quantum but not finished
Action: quantum += 2
Reason: Process needs more time
```

**Scenario 2: Priority Preemption (Phase 2)**
```
Condition: Higher priority process arrives during 25%-50%
Action: quantum += ceil(remainingQuantum / 2)
Reason: Partial quantum compensation
```

**Scenario 3: SJF Preemption (Phase 3)**
```
Condition: Shorter process arrives during 50%-100%
Action: quantum += remainingQuantum
Reason: Full quantum compensation
```

**Scenario 4: Process Completed**
```
Condition: Process finished before quantum exhausted
Action: quantum = 0
Reason: Process done
```

#### AG Example

**Initial State**:
```
P1: arrival=0, burst=17, priority=4, quantum=7
P2: arrival=2, burst=6, priority=7, quantum=9
P3: arrival=5, burst=11, priority=3, quantum=4
P4: arrival=15, burst=4, priority=6, quantum=6
```

**Execution Timeline**:

| Time | Event | Process | Quantum Before | Quantum After | Reason |
|------|-------|---------|----------------|---------------|--------|
| 0-4 | P1 runs | P1 | 7 | 7 | FCFS phase |
| 4 | P2 arrives, continues | P1 | 7 | 7 | In FCFS phase |
| 5 | P3 arrives (priority 3 > P1 priority 4) | P1→P3 | 7 | 10 | Priority preemption at 71% (Phase 3), so SJF rule: +3 |
| 7-9 | P2 runs | P2 | 9 | 12 | Used 2, then finished at 9 |
| 9-12 | P3 continues | P3 | 4 | 6 | Used all, +2 |
| 12-15 | P1 resumes | P1 | 10 | 14 | Used all, +2 (scenario 1) |
| 15-19 | P4 runs | P4 | 6 | 8 | Used 4, +2 |
| 19-21 | P3 resumes | P3 | 6 | 8 | Used 2, +2 |
| 21-26 | P1 runs | P1 | 14 | 0 | Completed |
| 26-36 | P4 continues | P4 | 8 | 0 | Completed |
| 36-38 | P3 finishes | P3 | 8 | 0 | Completed |

**Quantum History**:
```
P1: [7, 10, 14, 0]
P2: [9, 12, 0]
P3: [4, 6, 8, 0]
P4: [6, 8, 0]
```

**Execution Order**: `[P1, P2, P3, P2, P1, P3, P4, P3, P1, P4]`

**Performance Metrics**:
```
P1: Waiting=19, Turnaround=36
P2: Waiting=4, Turnaround=10
P3: Waiting=10, Turnaround=21
P4: Waiting=19, Turnaround=23

Average Waiting Time: 13.0
Average Turnaround Time: 22.5
```

**Use Case**: Adaptive systems requiring balanced fairness and efficiency

---

## 🏗️ System Architecture

### Project Structure

```
project3-cpu-scheduler/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── org/example/
│   │           └── Simulator.java          # Main implementation
│   │
│   └── test/
│       ├── java/
│       │   └── org/example/
│       │       └── SimulatorTest.java      # JUnit tests
│       │
│       └── resources/                       # Test case JSON files
│           ├── test_1.json                  # Non-AG test case 1
│           ├── test_2.json
│           ├── test_3.json
│           ├── test_4.json
│           ├── test_5.json
│           ├── test_6.json
│           ├── AG_test1.json                # AG test case 1
│           ├── AG_test2.json
│           ├── AG_test3.json
│           ├── AG_test4.json
│           ├── AG_test5.json
│           └── AG_test6.json
│
├── docs/
│   ├── Assignment_3.pdf                     # Assignment description
│   └── project_structure.png                # Visual structure
│
├── pom.xml                                   # Maven configuration
└── README.md                                 # This file
```

### Class Hierarchy

```
Simulator.java
├── Process                          # Process entity
├── ProcessResult                    # Metrics for completed process
├── SimulationResult                 # Overall simulation results
│
├── Scheduler Interface              # Common interface
│   ├── SJFScheduler                # Shortest Job First
│   ├── RoundRobinScheduler         # Round Robin
│   ├── PriorityScheduler           # Priority with aging
│   └── AGScheduler                 # AG hybrid
│
└── Test Data Classes
    ├── ProcessJSON                  # JSON input process
    ├── TestCase                     # Non-AG test structure
    └── AGTestCase                   # AG test structure
```

### Core Classes

#### Process
```java
class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int priority;         // For Priority + AG
    int quantum;          // For AG
    
    // Calculated metrics
    int completionTime;
    int waitingTime;
    int turnaroundTime;
}
```

#### SimulationResult
```java
class SimulationResult {
    List<String> executionOrder;
    List<ProcessResult> processResults;
    Map<String, List<Integer>> quantumHistory;  // AG only
    
    double averageWaitingTime;
    double averageTurnaroundTime;
}
```

#### Scheduler Interface
```java
interface Scheduler {
    SimulationResult simulate(List<Process> processes);
}
```

### Design Patterns

1. **Strategy Pattern**: Different scheduling algorithms implement same interface
2. **Builder Pattern**: JSON deserialization builds complex objects
3. **Template Method**: Common simulation structure, specific algorithm logic
4. **Dependency Injection**: Schedulers receive parameters via constructor

## 🚀 Installation & Usage

### Prerequisites

- **Java JDK**: Version 17 or higher
- **Maven**: Version 3.6+ (or use Maven Wrapper)
- **IDE** (Optional): IntelliJ IDEA, Eclipse, or VS Code with Java extensions

### Installation

```bash
# Clone or download the project
cd project3-cpu-scheduler

# Verify Maven installation
mvn --version
```

### Building the Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package (creates JAR file)
mvn package
```

### Running the Simulator

#### Method 1: Using Maven Exec Plugin

```bash
mvn exec:java -Dexec.mainClass="org.example.Simulator"
```

#### Method 2: Using Compiled JAR

```bash
# First package
mvn package

# Then run
java -cp target/scheduler-simulator-1.0-SNAPSHOT.jar org.example.Simulator
```

#### Method 3: From IDE

1. Open project in your IDE
2. Navigate to `src/main/java/org/example/Simulator.java`
3. Right-click → Run `Simulator.main()`

### Interactive Input

When you run the simulator, it prompts for:

```
Enter path for non-AG folder: src/test/resources/
Enter path for AG folder: src/test/resources/
```

**Folder Paths**:
- Point to directories containing JSON test files
- Use relative path: `src/test/resources/`
- Or absolute path: `/full/path/to/test/files/`

### Sample Run

```bash
$ mvn exec:java -Dexec.mainClass="org.example.Simulator"

Enter path for non-AG folder: src/test/resources/
Enter path for AG folder: src/test/resources/

--- SJF ---
Execution Order: [P1, P2, P4, P3, P2, P5, P1]
P1 | Waiting = 16 | Turnaround = 24
P2 | Waiting = 7 | Turnaround = 11
P3 | Waiting = 4 | Turnaround = 6
P4 | Waiting = 1 | Turnaround = 2
P5 | Waiting = 9 | Turnaround = 12
Average Waiting Time = 7.40
Average Turnaround Time = 11.00

--- RR ---
Execution Order: [P1, P2, P3, P1, P4, P5, P2, P1, P5, P1]
P1 | Waiting = 19 | Turnaround = 27
...

--- AG ---
Execution Order: [P1, P2, P3, P2, P1, P3, P4, P3, P1, P4]
P1 | Waiting = 19 | Turnaround = 36
Quantum History: [7, 10, 14, 0]
...
```

## 🧪 Test Cases

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SimulatorTest

# Run specific test method
mvn test -Dtest=SimulatorTest#testNonAG_case1

# Run with verbose output
mvn test -X
```

### Test Structure

The project includes **12 comprehensive test cases**:

**Non-AG Tests (test_1.json to test_6.json)**:
- Tests SJF, Round Robin, and Priority schedulers
- Each test validates:
  - Execution order
  - Individual process waiting/turnaround times
  - Average metrics

**AG Tests (AG_test1.json to AG_test6.json)**:
- Tests AG scheduler
- Additionally validates:
  - Quantum history for each process
  - Dynamic quantum updates

### Test Case Format

#### Non-AG Test (test_1.json)
```json
{
  "name": "Test Case 1: Basic mixed arrivals",
  "input": {
    "contextSwitch": 1,
    "rrQuantum": 2,
    "agingInterval": 5,
    "processes": [
      {"name": "P1", "arrival": 0, "burst": 8, "priority": 3},
      {"name": "P2", "arrival": 1, "burst": 4, "priority": 1}
    ]
  },
  "expectedOutput": {
    "SJF": {
      "executionOrder": ["P1", "P2", "P4", "P3", "P2", "P5", "P1"],
      "processResults": [
        {"name": "P1", "waitingTime": 16, "turnaroundTime": 24}
      ],
      "averageWaitingTime": 7.4,
      "averageTurnaroundTime": 11.0
    },
    "RR": { ... },
    "Priority": { ... }
  }
}
```

#### AG Test (AG_test1.json)
```json
{
  "input": {
    "processes": [
      {"name": "P1", "arrival": 0, "burst": 17, "priority": 4, "quantum": 7},
      {"name": "P2", "arrival": 2, "burst": 6, "priority": 7, "quantum": 9}
    ]
  },
  "expectedOutput": {
    "executionOrder": ["P1", "P2", "P3", "P2", "P1", "P3", "P4", "P3", "P1", "P4"],
    "processResults": [
      {
        "name": "P1",
        "waitingTime": 19,
        "turnaroundTime": 36,
        "quantumHistory": [7, 10, 14, 0]
      }
    ],
    "averageWaitingTime": 13.0,
    "averageTurnaroundTime": 22.5
  }
}
```

### JUnit Test Features

**Parameterized Tests**: Run same test logic for multiple inputs
```java
@ParameterizedTest(name = "Non-AG Test Case {0}")
@ValueSource(ints = {1, 2, 3, 4, 5, 6})
void testNonAGSchedulers(int testNumber) { ... }
```

**Display Names**: Descriptive test names
```java
@DisplayName("Test Non-AG Schedulers (SJF, RR, Priority)")
```

**Assertions**: Validate all aspects
```java
assertEquals(expected.executionOrder, actual.executionOrder);
assertEquals(expected.averageWaitingTime, actual.averageWaitingTime, 0.01);
```

### Creating Custom Tests

1. **Create JSON file** in `src/test/resources/`
2. **Define input** (processes, parameters)
3. **Define expected output** (execution order, metrics)
4. **Run tests** - JUnit automatically discovers new files

## 📊 Performance Metrics

### Metrics Calculated

#### 1. Waiting Time
```
Waiting Time = Turnaround Time - Burst Time
```
- Time process spends in ready queue
- Lower is better
- Affects user experience

#### 2. Turnaround Time
```
Turnaround Time = Completion Time - Arrival Time
```
- Total time from arrival to completion
- Includes waiting + execution time
- Lower is better

#### 3. Average Waiting Time
```
Average WT = (Sum of all Waiting Times) / Number of Processes
```
- Key performance indicator
- Primary optimization target

#### 4. Average Turnaround Time
```
Average TAT = (Sum of all Turnaround Times) / Number of Processes
```
- Overall system efficiency metric
- Balances short and long processes

### Example Calculation

**Process Set**:
```
P1: Arrival=0, Burst=8
P2: Arrival=1, Burst=4
P3: Arrival=2, Burst=2
```

**SJF Execution**:
```
Timeline: P1(0-1) → P3(2-4) → P2(4-8) → P1(8-15)

P1: Completion=15, TAT=15-0=15, WT=15-8=7
P2: Completion=8, TAT=8-1=7, WT=7-4=3
P3: Completion=4, TAT=4-2=2, WT=2-2=0

Average WT = (7+3+0)/3 = 3.33
Average TAT = (15+7+2)/3 = 8.00
```

### Comparing Algorithms

Typical performance characteristics:

| Algorithm | Avg WT | Avg TAT | Starvation Risk | Context Switches |
|-----------|--------|---------|-----------------|------------------|
| SJF | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | High | Medium |
| RR | ⭐⭐⭐ | ⭐⭐⭐ | None | High |
| Priority | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Low (aging) | Medium |
| AG | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | None | High |

**Legend**: ⭐⭐⭐⭐⭐ = Best, ⭐ = Worst

## 🔬 AG Scheduler Deep Dive

### Why AG Scheduling?

Traditional schedulers have trade-offs:
- **SJF**: Optimal average WT but can starve long processes
- **RR**: Fair but high context switching overhead
- **Priority**: Good for mixed workloads but complex tuning

**AG Scheduler** combines strengths while mitigating weaknesses.

### Three-Phase Execution Logic

```java
int quantum = process.quantum;
int timeUsed = currentQElapsed;
int time25 = ceil(quantum * 0.25);
int time50 = 2 * time25;

if (timeUsed == time25) {
    // Phase 2: Check for higher priority process
    if (higherPriorityExists()) {
        preempt();
        quantum += ceil(remainingQuantum / 2);
    }
} else if (timeUsed >= time50) {
    // Phase 3: Check for shorter job
    if (shorterJobExists()) {
        preempt();
        quantum += remainingQuantum;
    }
}
```

### Quantum Update Examples

**Example 1: Full Quantum Usage**
```
Initial quantum: 7
Process runs: 7 time units
Remaining work: Yes
New quantum: 7 + 2 = 9
```

**Example 2: Priority Preemption at 25%**
```
Initial quantum: 9
Process runs: 3 time units (33%, in Phase 2)
Preempted by: Higher priority process
Remaining: 9 - 3 = 6
New quantum: 9 + ceil(6/2) = 9 + 3 = 12
```

**Example 3: SJF Preemption at 50%**
```
Initial quantum: 10
Process runs: 7 time units (70%, in Phase 3)
Preempted by: Shorter job
Remaining: 10 - 7 = 3
New quantum: 10 + 3 = 13
```

**Example 4: Process Completes**
```
Initial quantum: 6
Process runs: 4 time units
Remaining work: No (finished)
New quantum: 0
```

### Advantages of AG

1. **Adaptive**: Quantum adjusts to process needs
2. **Fair**: Prevents starvation (like RR)
3. **Efficient**: Uses SJF logic when beneficial
4. **Priority-Aware**: Considers process importance
5. **Reduces Context Switching**: Compared to pure RR

### Disadvantages of AG

1. **Complexity**: More complex than traditional schedulers
2. **Overhead**: Multiple phase checks and quantum updates
3. **Tuning**: Initial quantum values affect performance
4. **Predictability**: Less predictable than simple RR

## 📚 Dependencies

### Maven Dependencies

**Jackson (JSON Processing)**:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.17.1</version>
</dependency>
```
- Parses JSON test files
- Deserializes to Java objects
- High-performance JSON library

**JUnit 5 (Testing)**:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
</dependency>
```
- Modern testing framework
- Parameterized tests
- Assertions and lifecycle management

**Maven Surefire (Test Runner)**:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
</plugin>
```
- Executes JUnit tests during `mvn test`
- Generates test reports

## 🎓 Academic Information

**Assignment Details**:
- **Course**: Operating Systems 1
- **Assignment**: Assignment 3 - CPU Schedulers Simulator
- **Maximum Score**: 125 marks (100 + 25 bonus + 10 unit tests)
- **Grading Breakdown**:
  - SJF Scheduler: 6 marks
  - Round Robin: 6 marks
  - Priority Scheduler: 6 marks
  - AG Scheduler: 31 marks
    - Execution order: 7 marks
    - Waiting time: 8 marks
    - Turnaround time: 8 marks
    - Quantum history: 8 marks
  - Average WT: 13 marks
  - Average TAT: 13 marks
  - Quantum history printing: 10 marks
  - Unit Tests (Bonus): 25 marks

**Learning Objectives**:
- Understand CPU scheduling algorithms
- Implement preemptive vs non-preemptive scheduling
- Calculate performance metrics
- Prevent process starvation
- Design hybrid scheduling algorithms
- Write comprehensive unit tests
- Use professional build tools (Maven)

## 💡 Implementation Highlights

### Context Switching

```java
// SJF with context switching
if (last != null && shortest != last) {
    time += contextSwitch;  // Add switching overhead
}
```

### Priority Aging

```java
// Increase priority of waiting processes
int waitedDuration = currentTime - lastPriorityUpdate;
if (waitedDuration >= agingInterval) {
    int decrement = waitedDuration / agingInterval;
    process.priority = Math.max(1, process.priority - decrement);
}
```

### Dynamic Quantum Management

```java
// AG: Quantum update on full usage
if (currentQElapsed == process.quantum && process.remainingTime > 0) {
    process.quantum += 2;
    quantumHistory.add(process.quantum);
    readyQueue.add(process);
}
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Tests fail with "File not found"
```
Solution: Ensure test JSON files are in src/test/resources/
Check Maven has copied resources to target/test-classes/
```

**Issue**: Maven command not recognized
```
Solution: Install Maven or use Maven Wrapper (./mvnw instead of mvn)
```

**Issue**: Java version mismatch
```
Solution: Ensure JDK 17+ is installed and JAVA_HOME is set
Update pom.xml if using different Java version
```

**Issue**: Tests pass but wrong output in main
```
Solution: Verify folder paths point to src/test/resources/
Use absolute paths if relative paths don't work
```

**Issue**: Jackson JSON parsing errors
```
Solution: Validate JSON files with online JSON validator
Ensure all required fields are present
Check for typos in field names
```

## 📝 Code Quality

### Best Practices Used

- ✅ **Interface-based design**: Scheduler interface for polymorphism
- ✅ **Deep copying**: Prevents test interference
- ✅ **Stream API**: Modern Java collections processing
- ✅ **Comparators**: Clean sorting logic
- ✅ **Maps for tracking**: Efficient quantum history storage
- ✅ **Descriptive names**: Clear variable and method names
- ✅ **Comments**: Complex logic explained
- ✅ **Consistent formatting**: Professional code style

### Testing Best Practices

- ✅ **Parameterized tests**: Test multiple cases efficiently
- ✅ **Descriptive test names**: Clear test purpose
- ✅ **Comprehensive assertions**: Validate all outputs
- ✅ **Test isolation**: Each test independent
- ✅ **Test data separation**: JSON files in resources
- ✅ **Expected vs Actual**: Clear failure messages

## 🤝 Contributing

This is an academic project. For students:
- Study the implementation to understand scheduling concepts
- Experiment with different parameters
- Create custom test cases
- Implement your own scheduler variations
- **Do not copy for academic submissions**

## 📄 License

Educational project for Operating Systems 1 course at Cairo University.

---

**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Course**: Operating Systems 1  
**Assignment**: CPU Schedulers Simulator  
**Build Tool**: Maven  
**Language**: Java 17+  
**Testing**: JUnit 5
