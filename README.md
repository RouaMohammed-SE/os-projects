# Operating Systems Projects Collection

A comprehensive collection of three operating system projects demonstrating core OS concepts including file system operations, process synchronization, and CPU scheduling algorithms. Built for CS241/CS352 Operating Systems course at Cairo University.


## 📋 Table of Contents
- [Overview](#overview)
- [Projects Summary](#projects-summary)
- [Quick Start](#quick-start)
- [Repository Structure](#repository-structure)
- [Technologies Used](#technologies-used)
- [Learning Outcomes](#learning-outcomes)
- [Getting Started](#getting-started)

## 🎯 Overview

This repository contains three progressively complex operating systems projects that cover fundamental concepts taught in undergraduate OS courses:

1. **Command Line Interpreter (CLI)** - File system operations and shell commands
2. **Car Wash Simulation** - Process synchronization using semaphores
3. **CPU Scheduler Simulator** - Implementation of 4 scheduling algorithms

Each project demonstrates professional software engineering practices, including proper documentation, testing, and adherence to OOP principles.

**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Courses**: CS241 (Operating Systems 1), CS352 (Advanced Software Engineering)  
**Language**: Java 17+  
**Total Projects**: 3  
**Total Lines of Code**: ~2,500+

## 📚 Projects Summary

### [Project 1: Command Line Interpreter](project1-cli/)

**Type**: Command-line application  
**Key Concepts**: File I/O, path resolution, command parsing

A fully-featured CLI that simulates a Unix-like terminal with 15+ commands including navigation (`cd`, `pwd`, `ls`), file operations (`cp`, `rm`, `cat`), directory management (`mkdir`, `rmdir`), compression utilities (`zip`, `unzip`), and I/O redirection (`>`, `>>`).

**Highlights**:
- ✅ 15+ implemented commands
- ✅ Absolute and relative path support
- ✅ I/O redirection operators
- ✅ Compression/decompression
- ✅ Robust error handling

**Assignment Score**: 24 marks (scaled to 6)

---

### [Project 2: Car Wash & Gas Station Simulation](project2-synchronization/)

**Type**: Multi-threaded application (Console + JavaFX GUI)    
**Key Concepts**: Threads, semaphores, producer-consumer, race conditions

A producer-consumer problem implementation simulating a car wash with bounded buffer (waiting area) and concurrent service bays. Features custom semaphore implementation, thread-safe queue operations, and real-time process synchronization.

**Highlights**:
- ✅ Custom semaphore class (P/V operations)
- ✅ Producer-consumer pattern
- ✅ 4 semaphores (mutex, empty, full, pumps)
- ✅ Prevents race conditions & deadlocks
- ✅ GUI version with real-time visualization (bonus)

**Assignment Score**: 70 marks (60 + 10 bonus for GUI)

---

### [Project 3: CPU Scheduler Simulator](project3-cpu-scheduler/)

**Type**: Maven project with JUnit testing   
**Key Concepts**: CPU scheduling, context switching, priority aging, quantum management

A comprehensive CPU scheduling simulator implementing 4 algorithms: Preemptive SJF, Round Robin, Priority with Aging, and AG (hybrid) Scheduling. Includes dynamic quantum management, starvation prevention, and complete performance metrics calculation.

**Highlights**:
- ✅ 4 scheduling algorithms
- ✅ AG scheduler with 3-phase execution
- ✅ Dynamic quantum updates (4 scenarios)
- ✅ Priority aging to prevent starvation
- ✅ 12+ automated JUnit test cases
- ✅ Maven build system

**Assignment Score**: 125 marks (100 + 25 unit test bonus)

---

## 🗂️ Repository Structure

```
os-projects/
│
├── project1-cli/                          # Command Line Interpreter
│   ├── src/
│   │   └── Terminal.java                  # Main CLI implementation
│   ├── docs/
│   │   └── Assignment_1.pdf
│   └── README.md                          # Project 1 documentation
│
├── project2-synchronization/              # Car Wash Simulation
│   ├── src/
│   │   ├── ServiceStation.java            # Console version
│   │   └── ServiceStationGUI.java         # JavaFX GUI version
│   ├── docs/
│   │   ├── Assignment_2.pdf
│   │   └── GstasionIcon.png
│   └── README.md                          # Project 2 documentation
│
├── project3-cpu-scheduler/                # CPU Scheduler Simulator (Maven)
│   ├── src/
│   │   ├── main/
│   │   │   └── java/org/example/
│   │   │       └── Simulator.java
│   │   └── test/
│   │       ├── java/org/example/
│   │       │   └── SimulatorTest.java
│   │       └── resources/
│   │           ├── test_1.json ... test_6.json       # Non-AG tests
│   │           └── AG_test1.json ... AG_test6.json   # AG tests
│   ├── docs/
│   │   ├── Assignment_3.pdf
│   │   └── project_structure.png
│   ├── pom.xml
│   └── README.md                          # Project 3 documentation
│
├── docs/                                  # Shared documentation
│   ├── screenshots/
│   └── diagrams/
│
├── README.md                              # This file (master README)
├── .gitignore                             # Git ignore rules
└── LICENSE                                # MIT License
```

## 🛠️ Technologies Used

### Languages & Frameworks
- **Java 17+**: Core programming language
- **JavaFX**: GUI framework (Project 2)
- **Maven**: Build automation (Project 3)
- **JUnit 5**: Unit testing framework (Project 3)

### Libraries & Tools
- **Java NIO**: File system operations (Project 1)
- **Java Concurrency**: Threading and synchronization (Project 2)
- **Jackson**: JSON parsing (Project 3)
- **Java Streams**: Functional programming

### Development Tools
- **Git**: Version control
- **Maven**: Dependency management
- **JUnit**: Automated testing
- **IntelliJ IDEA / Eclipse / VS Code**: IDEs

## 🎓 Learning Outcomes

### Operating Systems Concepts

✅ **File System Operations** (Project 1)
- Directory traversal and navigation
- File I/O and manipulation
- Path resolution (absolute/relative)
- Stream processing

✅ **Process Synchronization** (Project 2)
- Semaphores (binary and counting)
- Mutex locks and critical sections
- Producer-consumer problem
- Deadlock prevention
- Race condition handling

✅ **CPU Scheduling** (Project 3)
- Preemptive vs non-preemptive scheduling
- Context switching overhead
- Performance metrics (WT, TAT)
- Starvation prevention (aging)
- Hybrid scheduling algorithms

### Software Engineering Skills

✅ **Clean Code**
- Modular design with clear separation of concerns
- Meaningful variable and method names
- Comprehensive comments and documentation

✅ **Testing**
- Unit testing with JUnit 5
- Parameterized tests
- JSON-based test cases
- Automated validation

✅ **Professional Tools**
- Maven build system
- Git version control
- Professional project structure
- README documentation

✅ **Design Principles**
- SOLID principles (especially in restaurant project)
- Interface-based programming
- Design patterns (Strategy, Template, Builder, etc.)

## 🚀 Quick Start

### Prerequisites

**All Projects**:
```bash
java --version  # Java 17 or higher required
```

**Project 3 Only**:
```bash
mvn --version   # Maven 3.6+ required
```

**Project 2 GUI Only**:
```bash
# JavaFX SDK 17+ required
```

### Running Projects

#### Project 1: CLI
```bash
cd project1-cli/src
javac Terminal.java
java Terminal

# Interactive CLI starts
> pwd
> ls
> mkdir test
> cd test
> exit
```

#### Project 2: Car Wash Simulation

**Console Version**:
```bash
cd project2-synchronization/src
javac ServiceStation.java
java ServiceStation

# Enter configuration:
# Waiting area capacity: 5
# Number of pumps: 3
# Cars: C1 C2 C3 C4 C5
```

**GUI Version**:
```bash
# Requires JavaFX setup
javac --module-path /path/to/javafx/lib --add-modules javafx.controls ServiceStation.java
java --module-path /path/to/javafx/lib --add-modules javafx.controls ServiceStation
```

#### Project 3: CPU Scheduler
```bash
cd project3-cpu-scheduler

# Build and test
mvn clean test

# Run simulator
mvn exec:java -Dexec.mainClass="org.example.Simulator"

# Enter test file paths when prompted
```

## 📊 Projects Comparison

| Feature | Project 1 | Project 2 | Project 3 |
|---------|-----------|-----------|-----------|
| **Type** | CLI App | Multi-threaded | Maven + Tests |
| **Complexity** | Medium | High | Very High |
| **Lines of Code** | ~650 | ~500 + ~400 (GUI) | ~900 + ~300 (tests) |
| **Key Concept** | File I/O | Synchronization | Scheduling |
| **Testing** | Manual | Manual | Automated (JUnit) |
| **GUI** | ❌ | ✅ (Bonus) | ❌ |
| **External Deps** | None | JavaFX | Jackson, JUnit |

## 📝 Documentation

Each project has its own detailed README covering:
- ✅ Installation instructions
- ✅ Usage examples
- ✅ Implementation details
- ✅ Code architecture
- ✅ Test cases
- ✅ Troubleshooting

**Master Docs**: This file provides overview  
**Project Docs**: Individual READMEs provide depth  
**Assignment PDFs**: Original requirements in `docs/` folders

## 🔧 Development Setup

### Clone Repository
```bash
git clone https://github.com/yourusername/os-projects.git
cd os-projects
```

### Setup Projects

**Project 1** (No setup needed):
```bash
cd project1-cli/src
javac Terminal.java
```

**Project 2** (Setup JavaFX for GUI):
```bash
# Download JavaFX SDK from openjfx.io
# Set PATH and configure IDE
```

**Project 3** (Maven project):
```bash
cd project3-cpu-scheduler
mvn clean install
```

### IDE Configuration

**IntelliJ IDEA**:
1. File → Open → Select `os-projects` folder
2. Trust project
3. Maven projects auto-detected
4. For JavaFX: File → Project Structure → Libraries → Add JavaFX SDK

**Eclipse**:
1. File → Import → Existing Maven Projects
2. Select `project3-cpu-scheduler`
3. For regular Java: Import as Java Projects

**VS Code**:
1. Install Java Extension Pack
2. Install Maven for Java
3. Open folder: `os-projects`

## 🧪 Testing

### Project 1: Manual Testing
```bash
# Test each command individually
# Verify output matches expected behavior
# Check error handling with invalid inputs
```

### Project 2: Manual Testing
```bash
# Verify thread synchronization
# Check queue operations
# Test context switching
# Observe GUI updates (GUI version)
```

### Project 3: Automated Testing
```bash
cd project3-cpu-scheduler

# Run all tests
mvn test

# Run specific test suite
mvn test -Dtest=SimulatorTest#testNonAGSchedulers

# View test results
cat target/surefire-reports/*.txt
```

**Test Coverage**:
- ✅ 6 Non-AG test cases (SJF, RR, Priority)
- ✅ 6 AG test cases (AG scheduler)
- ✅ 12 total automated scenarios

## 🎯 Key Achievements

### Technical Excellence
- ✅ Professional code structure across all projects
- ✅ Comprehensive error handling
- ✅ Clean, documented, maintainable code
- ✅ Automated testing (Project 3)
- ✅ Maven build system (Project 3)

### OS Concepts Mastered
- ✅ File system operations and I/O
- ✅ Multi-threading and synchronization
- ✅ Semaphores and mutex locks
- ✅ CPU scheduling algorithms
- ✅ Context switching
- ✅ Performance metric calculation

### Bonus Features
- ✅ JavaFX GUI for car wash simulation (+10 marks)
- ✅ JUnit unit tests for schedulers (+25 marks)
- ✅ Compression utilities (zip/unzip)
- ✅ Priority aging for starvation prevention
- ✅ Dynamic quantum management (AG scheduler)

## 💻 Example Outputs

### Project 1: CLI Session
```
> pwd
Current directory: /home/user

> mkdir test-folder
Directory created: /home/user/test-folder

> cd test-folder
Changed directory to: /home/user/test-folder

> touch file1.txt
File created: file1.txt

> ls
file1.txt

> exit
```

### Project 2: Car Wash
```
C1 arrived
C2 arrived
Pump 1: C1 Occupied
Pump 2: C2 Occupied
C3 arrived and waiting
Pump 1: C1 begins service at Bay 1
Pump 1: C1 finishes service
Pump 1: Bay 1 is now free
Pump 1: C3 Occupied
```

### Project 3: Scheduler Output
```
--- SJF ---
Execution Order: [P1, P2, P4, P3, P2, P5, P1]
P1 | Waiting = 16 | Turnaround = 24
P2 | Waiting = 7 | Turnaround = 11
Average Waiting Time = 7.40

--- AG ---
Execution Order: [P1, P2, P3, P2, P1, P3, P4, P3, P1, P4]
P1 Quantum History: [7, 10, 14, 0]
Average Waiting Time = 13.00
```

## 📄 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) for details.

## 👥 Authors

**Students**: Roaa Mohammed, Mennat-Allah Abdallah, Peter Boshra,Emil Ehab, Peter Gergis
**Course**: Operating Systems 1
**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Academic Year**: 2025
