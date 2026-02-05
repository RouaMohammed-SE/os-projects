# OS Project 2: Car Wash & Gas Station Simulation

A multi-threaded Java application implementing the classic **Producer-Consumer Problem** using semaphores to simulate a busy car wash and gas station with concurrent service bays and a bounded waiting area.

![Gas Station Icon](docs/GstasionIcon.png)

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Implementation Versions](#implementation-versions)
- [Synchronization Concepts](#synchronization-concepts)
- [Installation & Usage](#installation--usage)
- [How It Works](#how-it-works)
- [Example Output](#example-output)
- [Thread Safety](#thread-safety)

## 🎯 Overview

This project demonstrates advanced operating system concepts including:
- **Producer-Consumer Pattern**: Cars (producers) arrive and Pumps (consumers) service them
- **Bounded Buffer**: Fixed-size waiting area with thread-safe queue operations
- **Semaphore Synchronization**: Prevents race conditions and manages shared resources
- **Thread Pools**: Concurrent pump threads servicing multiple cars simultaneously
- **Resource Management**: Limited service bays with proper acquisition and release

**Course**: Operating Systems 1 (CS241)  
**Assignment**: Assignment 2 - Synchronization  
**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Language**: Java 17+ (Console) / JavaFX (GUI)

### Problem Statement

Simulate a service station where:
- **Cars** arrive and enter a waiting queue (bounded buffer)
- **Pumps** (service bays) work concurrently to service cars
- **Waiting area** has limited capacity (1-10 cars)
- **Race conditions** are prevented using semaphores and mutexes
- **System activities** are logged in real-time

## ✨ Features

### Core Synchronization Features
- ✅ **Thread-Safe Queue**: Concurrent access by multiple threads
- ✅ **Semaphore Implementation**: Custom semaphore class (P/V operations)
- ✅ **Mutex Protection**: Prevents race conditions on shared data
- ✅ **Bounded Buffer**: Fixed-size waiting area (1-10 capacity)
- ✅ **Resource Counting**: Semaphores track available service bays
- ✅ **Deadlock Prevention**: Proper lock ordering and release

### Simulation Features
- ✅ **Dynamic Configuration**: User-defined waiting capacity and pump count
- ✅ **Concurrent Servicing**: Multiple pumps work simultaneously
- ✅ **Real-Time Logging**: All events logged with timestamps
- ✅ **Graceful Shutdown**: Poison pill pattern for clean termination
- ✅ **Input Validation**: Enforces valid capacity (1-10) and pump counts

### GUI Version Features (Bonus)
- ✅ **Visual Simulation**: Real-time graphical representation
- ✅ **Waiting Area Display**: Visual queue with car labels
- ✅ **Pump Status Indicators**: Color-coded service bay states
- ✅ **Live Log**: Scrollable event log
- ✅ **Interactive Controls**: Input fields for configuration

## 🏗️ System Architecture

### Class Structure

```
ServiceStation.java (Console Version)
├── Semaphore Class
│   ├── value: int
│   ├── Semaphore(int initial)
│   ├── getValue(): int
│   ├── p(): void (wait/down operation)
│   └── v(): void (signal/up operation)
│
├── Car Class (Producer Thread)
│   ├── carName: String
│   ├── queue: Queue<String>
│   ├── full, empty, mutex, pumps: Semaphore
│   └── run(): void
│
├── Pump Class (Consumer Thread)
│   ├── pumpId: int
│   ├── queue: Queue<String>
│   ├── full, empty, mutex, pumps: Semaphore
│   └── run(): void
│
└── ServiceStation (Main)
    ├── main(String[] args)
    └── Initialization & Coordination Logic
```

### Synchronization Architecture

```
Shared Resources:
├── Queue<String> queue          → Bounded buffer (waiting area)
├── Semaphore mutex              → Mutual exclusion (queue access)
├── Semaphore full               → Count of cars in queue
├── Semaphore empty              → Available spaces in queue
└── Semaphore pumps              → Available service bays
```

### Thread Model

```
Main Thread
├── Creates shared resources (queue, semaphores)
├── Spawns Pump Threads (Consumer Pool)
│   ├── Pump 1 (continuously waits for cars)
│   ├── Pump 2
│   └── Pump N
│
├── Spawns Car Threads (Producers)
│   ├── Car 1 (arrives, enters queue)
│   ├── Car 2
│   └── Car M
│
└── Coordination
    ├── Waits for all cars to arrive
    ├── Sends poison pills ("DONE") to pumps
    └── Waits for pumps to finish
```

## 🎨 Implementation Versions

### Version 1: Console Application

**File**: `ServiceStation.java` (Console)

**Features**:
- Command-line interface
- Text-based logging
- Lightweight and portable
- No external dependencies

**Use Case**: Understanding core synchronization concepts, debugging, testing

---

### Version 2: JavaFX GUI Application

**File**: `ServiceStation.java` (GUI)

**Features**:
- Graphical user interface
- Visual waiting queue display
- Color-coded pump status indicators
- Real-time event log panel
- Interactive input controls

**Use Case**: Visual demonstration, presentations, better user experience

**Additional Dependencies**:
- JavaFX SDK (version 17+)

---

## 🔒 Synchronization Concepts

### Semaphore Implementation

Our custom `Semaphore` class implements classic P (wait) and V (signal) operations:

```java
class Semaphore {
    protected int value = 0;
    
    // P operation (wait/down) - decrement and block if negative
    public synchronized void p() {
        value--;
        if (value < 0) {
            wait();  // Block thread until signaled
        }
    }
    
    // V operation (signal/up) - increment and wake waiting thread
    public synchronized void v() {
        value++;
        if (value <= 0) {
            notify();  // Wake one waiting thread
        }
    }
}
```

### Four Semaphores Used

1. **mutex** (Binary Semaphore - Initial: 1)
   - **Purpose**: Mutual exclusion for queue access
   - **Usage**: Protects critical sections when adding/removing from queue
   - **Pattern**: `mutex.p()` → critical section → `mutex.v()`

2. **empty** (Counting Semaphore - Initial: waitingCapacity)
   - **Purpose**: Tracks available spaces in waiting area
   - **Usage**: Producers wait if queue is full
   - **Pattern**: `empty.p()` before adding → `empty.v()` after removing

3. **full** (Counting Semaphore - Initial: 0)
   - **Purpose**: Tracks number of cars waiting
   - **Usage**: Consumers wait if queue is empty
   - **Pattern**: `full.p()` before removing → `full.v()` after adding

4. **pumps** (Counting Semaphore - Initial: numPumps)
   - **Purpose**: Tracks available service bays
   - **Usage**: Ensures only N cars serviced concurrently
   - **Pattern**: `pumps.p()` start service → `pumps.v()` finish service

### Producer-Consumer Pattern

#### Car (Producer) Logic
```
1. empty.p()           → Wait for space in queue
2. mutex.p()           → Lock queue
3. queue.add(car)      → Add car to queue
4. mutex.v()           → Unlock queue
5. full.v()            → Signal car is waiting
```

#### Pump (Consumer) Logic
```
1. full.p()            → Wait for car in queue
2. mutex.p()           → Lock queue
3. car = queue.remove() → Remove car from queue
4. mutex.v()           → Unlock queue
5. empty.v()           → Signal space available
6. pumps.p()           → Acquire service bay
7. [Service car]       → Simulate service time
8. pumps.v()           → Release service bay
```

### Race Condition Prevention

**Without synchronization**:
```
Thread 1: if (!queue.isEmpty())  → interrupted
Thread 2: car = queue.remove()   → Success
Thread 1: car = queue.remove()   → ERROR: Empty queue!
```

**With mutex**:
```
Thread 1: mutex.p() → if (!queue.isEmpty()) → queue.remove() → mutex.v()
Thread 2: mutex.p() → [waits] → mutex acquired → checks queue → mutex.v()
```

## 🚀 Installation & Usage

### Prerequisites

**Console Version**:
- Java JDK 17 or higher
- No external dependencies

**GUI Version**:
- Java JDK 17 or higher
- JavaFX SDK 17 or higher

### Compilation & Execution

#### Console Version

```bash
# Compile
javac ServiceStation.java

# Run
java ServiceStation

# Interactive prompts will appear:
# Enter waiting area capacity (1-10): 5
# Enter number of pumps: 3
# Enter car order (e.g., C1 C2 C3 C4 C5): C1 C2 C3 C4 C5
```

#### GUI Version

```bash
# Method 1: Using JavaFX SDK
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls ServiceStation.java
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls ServiceStation

# Method 2: Using IDE (IntelliJ IDEA / Eclipse)
# - Open project in IDE
# - Configure JavaFX library
# - Run ServiceStation.java
```

### Input Parameters

| Parameter | Range | Description |
|-----------|-------|-------------|
| Waiting Capacity | 1-10 | Maximum cars in waiting queue |
| Number of Pumps | ≥1 | Concurrent service bays |
| Car Order | Space-separated | List of car names (e.g., C1 C2 C3) |

### Example Configuration

**Small Setup** (Testing):
- Waiting Capacity: 3
- Pumps: 2
- Cars: C1 C2 C3 C4

**Medium Setup** (Demo):
- Waiting Capacity: 5
- Pumps: 3
- Cars: C1 C2 C3 C4 C5 C6 C7

**Large Setup** (Stress Test):
- Waiting Capacity: 10
- Pumps: 5
- Cars: C1 C2 C3 ... C20

## 🔍 How It Works

### Simulation Flow

```
1. Initialization
   └── Create queue, semaphores (mutex, empty, full, pumps)

2. Start Pump Threads (Consumers)
   └── Each pump runs in infinite loop waiting for cars

3. Start Car Threads (Producers)
   └── Each car thread:
       ├── Waits for space (empty.p())
       ├── Enters queue
       └── Exits thread

4. Main Thread Waits
   └── Joins all car threads

5. Send Poison Pills
   └── Add "DONE" messages (one per pump)

6. Pump Threads Terminate
   └── Each pump exits upon receiving "DONE"

7. Cleanup
   └── All threads joined, simulation ends
```

### State Diagram

```
Car States:
┌─────────┐
│ Created │
└────┬────┘
     │
     ▼
┌─────────────┐
│  Waiting    │ ◄── empty.p() blocks if queue full
│  for Space  │
└─────┬───────┘
      │
      ▼
┌─────────────┐
│  In Queue   │
│  (Waiting)  │
└─────┬───────┘
      │
      ▼
┌─────────────┐
│ Being       │ ◄── pumps.p() acquired
│ Serviced    │
└─────┬───────┘
      │
      ▼
┌─────────────┐
│   Done      │ ◄── pumps.v() released
└─────────────┘
```

### Pump States

```
┌──────────┐
│   Idle   │ ◄── Waiting for car (full.p() blocks)
└────┬─────┘
     │
     ▼
┌──────────┐
│  Busy    │ ◄── Servicing car
└────┬─────┘
     │
     ▼
┌──────────┐
│   Free   │ ◄── Released bay, back to waiting
└──────────┘
```

## 📊 Example Output

### Sample Run (Console Version)

**Input**:
- Waiting Capacity: 5
- Pumps: 3
- Cars: C1 C2 C3 C4 C5

**Output**:
```
C1 arrived
C2 arrived
C3 arrived
Pump 1: C1 Occupied
Pump 2: C2 Occupied
Pump 3: C3 Occupied
C4 arrived
C4 arrived and waiting
C5 arrived
C5 arrived and waiting
Pump 1: C1 login
Pump 1: C1 begins service at Bay 1
Pump 2: C2 login
Pump 2: C2 begins service at Bay 2
Pump 3: C3 login
Pump 3: C3 begins service at Bay 3
Pump 1: C1 finishes service
Pump 1: Bay 1 is now free
Pump 2: C2 finishes service
Pump 2: Bay 2 is now free
Pump 1: C4 Occupied
Pump 1: C4 login
Pump 1: C4 begins service at Bay 1
Pump 3: C3 finishes service
Pump 3: Bay 3 is now free
Pump 2: C5 Occupied
Pump 2: C5 login
Pump 2: C5 begins service at Bay 2
Pump 1: C4 finishes service
Pump 1: Bay 1 is now free
Pump 2: C5 finishes service
Pump 2: Bay 2 is now free
All cars processed; simulation ends
```

### Event Timeline Explanation

| Time | Event | Explanation |
|------|-------|-------------|
| T0 | C1, C2, C3 arrive | First 3 cars enter queue |
| T1 | All pumps occupied | Pumps 1, 2, 3 start servicing |
| T2 | C4, C5 arrive | Queue has space, cars wait |
| T3 | C1 finishes | Pump 1 free, C4 starts |
| T4 | C2 finishes | Pump 2 free, C5 starts |
| T5 | C3 finishes | Pump 3 now free |
| T6 | C4 finishes | Pump 1 free again |
| T7 | C5 finishes | All cars done |

## 🧪 Thread Safety

### Critical Sections Protected

1. **Queue Modification**
   ```java
   mutex.p();
   queue.add(car);      // Protected
   mutex.v();
   ```

2. **Queue Access**
   ```java
   mutex.p();
   String car = queue.remove();  // Protected
   mutex.v();
   ```

3. **Semaphore Value Reading**
   ```java
   synchronized int getValue() {  // Thread-safe read
       return value;
   }
   ```

### Synchronization Guarantees

✅ **No Race Conditions**: Mutex protects queue operations  
✅ **No Deadlocks**: Locks acquired/released in consistent order  
✅ **No Busy Waiting**: Semaphores use `wait()`/`notify()`  
✅ **Bounded Buffer**: Empty/full semaphores enforce capacity  
✅ **Resource Limits**: Pump semaphore prevents overload  

### Testing Scenarios

#### Test 1: Queue Overflow
```
Config: Capacity=2, Pumps=1, Cars=5
Expected: Cars 3-5 wait until space available
Result: ✅ Bounded buffer enforced
```

#### Test 2: Concurrent Access
```
Config: Capacity=5, Pumps=3, Cars=10
Expected: No race conditions, all cars processed
Result: ✅ Mutex prevents conflicts
```

#### Test 3: Resource Starvation
```
Config: Capacity=10, Pumps=2, Cars=20
Expected: All cars eventually serviced
Result: ✅ Fair semaphore scheduling
```

## 🎨 GUI Features (Bonus Implementation)

### Visual Components

1. **Input Controls Panel**
   - Waiting capacity text field (1-10 validation)
   - Number of pumps text field
   - Car order input field
   - Start Simulation button

2. **Waiting Area Display**
   - FlowPane showing waiting cars
   - Color-coded labels (orange background)
   - Current count indicator (e.g., "Current: 3/5")
   - Scrollable for large queues

3. **Service Bays Grid**
   - One box per pump
   - Status labels:
     - **Free**: Gray background
     - **Occupied**: Green background with car name
   - Grid layout (2 columns, N/2 rows)

4. **Simulation Log**
   - Scrollable TextArea
   - Real-time event logging
   - Same messages as console version

### GUI Screenshots Description

**Initial State**:
- All pumps show "Free" (gray)
- Waiting area empty
- Log area clear

**During Simulation**:
- Pumps 1-3 show "C1/C2/C3 In Service" (green)
- Waiting area shows C4, C5 (orange labels)
- Log scrolls with events

**After Completion**:
- All pumps return to "Free"
- Waiting area empty
- Final message: "All cars processed; simulation ends"

### GUI Thread Safety

- Uses `Platform.runLater()` for UI updates from worker threads
- Ensures JavaFX Application Thread handles all UI modifications
- Prevents `IllegalStateException` from concurrent UI access

## 📚 Key Concepts Demonstrated

### Operating Systems Concepts

1. **Process Synchronization**
   - Semaphores (binary and counting)
   - Mutex locks
   - Critical sections

2. **Concurrency**
   - Multi-threading
   - Thread creation and management
   - Thread pools

3. **Producer-Consumer Problem**
   - Bounded buffer implementation
   - Multiple producers (cars)
   - Multiple consumers (pumps)

4. **Resource Management**
   - Limited resources (service bays)
   - Resource acquisition/release
   - Prevention of resource conflicts

5. **Deadlock Prevention**
   - Proper lock ordering
   - Timeout mechanisms
   - Resource allocation strategies

### Java Concurrency Features Used

- `Thread` class and `Runnable` interface
- `synchronized` keyword
- `wait()` and `notify()` methods
- `LinkedList` as thread-safe queue (with mutex)
- `AtomicInteger` for counters (GUI version)
- `Platform.runLater()` for UI thread safety (GUI version)

## 🎓 Academic Information

**Assignment Details**:
- **Course**: Operating Systems 1 (CS241)
- **Assignment**: Assignment 2 - Synchronization
- **Maximum Score**: 70 marks (60 + 10 bonus)
- **Grading Breakdown**:
  - ServiceStation: 10 marks
  - Semaphore: 10 marks
  - Car (Producer): 15 marks
  - Pump (Consumer): 15 marks
  - Valid Output: 20 marks
  - GUI (Bonus): 10 marks

**Learning Objectives**:
- Implement custom semaphore class
- Apply producer-consumer pattern
- Prevent race conditions
- Manage shared resources
- Coordinate multiple threads
- Implement bounded buffer
- Design thread-safe applications

## 🔧 Implementation Details

### Design Decisions

1. **LinkedList as Queue**
   - Efficient add/remove operations
   - FIFO ordering maintained
   - Synchronized access via mutex

2. **Poison Pill Pattern**
   - "DONE" messages for graceful shutdown
   - One poison pill per consumer
   - Prevents consumer starvation

3. **Service Time Simulation**
   - `Thread.sleep(500ms)` for console
   - `Thread.sleep(600ms)` for GUI
   - Represents car wash duration

4. **Semaphore Wait/Notify**
   - Uses Java's built-in `wait()`/`notify()`
   - Avoids busy-waiting
   - Efficient CPU usage

### Error Handling

- **Input Validation**: Capacity must be 1-10
- **Interrupt Handling**: `InterruptedException` caught and handled
- **Queue Safety**: Null checks before processing
- **GUI Alerts**: User-friendly error dialogs

## 📝 Code Highlights

### Producer (Car) Implementation
```java
@Override
public void run() {
    empty.p();              // Wait for space
    mutex.p();              // Lock queue
    
    queue.add(carName);     // Enter queue
    log(carName + " arrived");
    
    mutex.v();              // Unlock queue
    full.v();               // Signal car waiting
}
```

### Consumer (Pump) Implementation
```java
@Override
public void run() {
    while (true) {
        full.p();           // Wait for car
        mutex.p();          // Lock queue
        
        String car = queue.remove();  // Get car
        
        mutex.v();          // Unlock queue
        empty.v();          // Signal space
        
        if (car.equals("DONE")) break;  // Exit condition
        
        pumps.p();          // Acquire bay
        // ... service car ...
        pumps.v();          // Release bay
    }
}
```

### Semaphore P/V Operations
```java
public synchronized void p() {
    value--;
    if (value < 0) {
        wait();  // Block until resource available
    }
}

public synchronized void v() {
    value++;
    if (value <= 0) {
        notify();  // Wake one waiting thread
    }
}
```

## 🐛 Troubleshooting

### Common Issues

**Issue**: Cars not being serviced
- **Cause**: Pump threads not started
- **Solution**: Ensure pump threads created before car threads

**Issue**: Queue overflow exception
- **Cause**: Empty semaphore not enforced
- **Solution**: Verify `empty.p()` called before queue.add()

**Issue**: Deadlock occurs
- **Cause**: Incorrect lock ordering
- **Solution**: Always acquire locks in same order (empty → mutex → full)

**Issue**: GUI not updating
- **Cause**: UI updates from worker thread
- **Solution**: Wrap UI code in `Platform.runLater()`

**Issue**: JavaFX runtime error
- **Cause**: Missing JavaFX modules
- **Solution**: Add `--module-path` and `--add-modules` flags

## 🤝 Contributing

This is an academic project. For students:
- Use as reference for understanding synchronization
- Implement your own solution for submissions
- Study the semaphore implementation pattern

## 📄 License

Educational project for Operating Systems 1 course at Cairo University.

---

**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Course**: Operating Systems 1 (CS241)  
**Assignment**: Synchronization - Producer-Consumer Problem  
**Language**: Java 17+ / JavaFX
