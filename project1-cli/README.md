# OS Project 1: Command Line Interpreter (CLI)

A fully-featured Command Line Interpreter built in Java that simulates a Unix-like shell environment with support for file operations, directory management, compression utilities, and I/O redirection.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Supported Commands](#supported-commands)
- [Installation & Usage](#installation--usage)
- [Command Examples](#command-examples)
- [Implementation Details](#implementation-details)
- [Error Handling](#error-handling)

## 🎯 Overview

This Command Line Interpreter provides a terminal-like interface for interacting with the operating system. It parses user commands, validates arguments, and executes various file system operations while maintaining proper error handling and user feedback.

**Course**: Operating Systems 1  
**Assignment**: Assignment 1 - Command Line Interpreter  
**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Language**: Java 17+

### Key Capabilities
- **File Operations**: Create, copy, delete, and read files
- **Directory Management**: Navigate, create, and remove directories
- **Compression**: Zip and unzip files and directories
- **I/O Redirection**: Redirect output to files with `>` and `>>`
- **Path Handling**: Support for both absolute and relative paths
- **Word Count**: Analyze file content (lines, words, characters)

## ✨ Features

### Core Functionality
- ✅ **Interactive Shell**: Continuous command input until `exit`
- ✅ **Path Management**: Tracks and navigates current working directory
- ✅ **Absolute & Relative Paths**: Full support for both path types
- ✅ **Error Handling**: Graceful error messages without termination
- ✅ **Alphabetical Sorting**: Directory listings sorted alphabetically
- ✅ **Recursive Operations**: Recursive directory copying and compression

### Advanced Features
- ✅ **Output Redirection**: `>` (overwrite) and `>>` (append)
- ✅ **Bulk Operations**: Create multiple directories at once
- ✅ **Empty Directory Cleanup**: Remove all empty directories with `rmdir *`
- ✅ **File Concatenation**: View and combine multiple file contents
- ✅ **Compression**: Create and extract ZIP archives with recursive support

## 🏗️ System Architecture

### Class Structure

```
Terminal.java
├── Parser Class
│   ├── commandName: String
│   ├── args: String[]
│   ├── parse(String input): void
│   ├── getCommandName(): String
│   └── getArgs(): String[]
│
└── Terminal Class
    ├── parser: Parser
    ├── scanner: Scanner
    ├── currentDirectory: Path
    │
    ├── Command Methods:
    │   ├── pwd()
    │   ├── cd(String[] args)
    │   ├── ls()
    │   ├── mkdir(String[] args)
    │   ├── rmdir(String[] args)
    │   ├── touch(String[] args)
    │   ├── cp(String[] args)
    │   ├── rm(String[] args)
    │   ├── cat(String[] args)
    │   ├── wc(String[] args)
    │   ├── zip(String[] args)
    │   └── unzip(String[] args)
    │
    ├── Helper Methods:
    │   ├── ReadFile(String fileName)
    │   ├── writeFile(String fileName, String content, boolean isAppend)
    │   ├── CountLines(String str)
    │   ├── CountWords(String str)
    │   ├── CountChars(String str)
    │   ├── zipFile(Path filePath, String zipEntryName, ZipOutputStream zos)
    │   ├── zipDirectory(Path folderPath, String basePath, ZipOutputStream zos)
    │   └── executeWithRedirection(...)
    │
    ├── chooseCommandAction(): void
    └── main(String[] args): void
```

### Design Principles

1. **Separation of Concerns**: Parser handles input parsing, Terminal handles execution
2. **Modularity**: Each command is a separate method
3. **Error Handling**: Try-catch blocks with informative error messages
4. **Path Abstraction**: Uses Java NIO Path API for robust path handling
5. **State Management**: Maintains current directory state throughout session

## 📚 Supported Commands

### Navigation & Information

#### `pwd` - Print Working Directory
Displays the current directory path.

**Syntax:**
```bash
pwd
```

**Examples:**
```bash
> pwd
Current directory: /home/user/documents

> pwd > output.txt    # Redirect to file
```

**Features:**
- No arguments required
- Supports output redirection (`>`, `>>`)

---

#### `cd` - Change Directory
Changes the current working directory.

**Syntax:**
```bash
cd                    # Go to home directory
cd ..                 # Go to parent directory
cd <path>             # Go to specific directory
```

**Examples:**
```bash
> cd
Changed directory to home: /home/user

> cd ..
Moved up to parent: /home

> cd documents/projects
Changed directory to: /home/user/documents/projects

> cd /absolute/path/to/directory
Changed directory to: /absolute/path/to/directory
```

**Features:**
- No arguments → navigate to home directory
- `..` → navigate to parent directory
- Supports absolute and relative paths
- Error handling for non-existent directories

---

#### `ls` - List Directory Contents
Lists all files and directories in the current directory, sorted alphabetically.

**Syntax:**
```bash
ls
```

**Examples:**
```bash
> ls
Documents
Downloads
Pictures
file1.txt
file2.txt

> ls > files.txt      # Redirect to file
> ls >> files.txt     # Append to file
```

**Features:**
- Alphabetically sorted output
- Supports output redirection

---

### Directory Operations

#### `mkdir` - Make Directory
Creates one or more directories.

**Syntax:**
```bash
mkdir <dir1> [dir2] [dir3] ...
```

**Examples:**
```bash
> mkdir newFolder
Directory created: /current/path/newFolder

> mkdir folder1 folder2 folder3
Directory created: /current/path/folder1
Directory created: /current/path/folder2
Directory created: /current/path/folder3

> mkdir /absolute/path/newfolder
Directory created: /absolute/path/newfolder

> mkdir ../parent-folder
Directory created: /parent/path/parent-folder
```

**Features:**
- Creates multiple directories in one command
- Supports absolute and relative paths
- Creates parent directories if needed
- Prevents overwriting existing files or directories

---

#### `rmdir` - Remove Directory
Removes empty directories.

**Syntax:**
```bash
rmdir <directory>     # Remove specific empty directory
rmdir *               # Remove all empty directories in current directory
```

**Examples:**
```bash
> rmdir emptyFolder
Removed directory: emptyFolder

> rmdir *
Removed 3 empty directories

> rmdir /path/to/empty/dir
Removed directory: /path/to/empty/dir
```

**Features:**
- Only removes empty directories (safety feature)
- `*` removes all empty directories in current location
- Error messages for non-empty directories

---

### File Operations

#### `touch` - Create File
Creates a new empty file.

**Syntax:**
```bash
touch <filename>
```

**Examples:**
```bash
> touch newfile.txt
File created: newfile.txt

> touch /absolute/path/file.txt
File created: /absolute/path/file.txt

> touch ../sibling/file.txt
File created: ../sibling/file.txt
```

**Features:**
- Creates empty files
- Supports absolute and relative paths
- Creates parent directories if needed

---

#### `rm` - Remove File
Deletes a file from the current directory.

**Syntax:**
```bash
rm <filename>
```

**Examples:**
```bash
> rm oldfile.txt
File removed: oldfile.txt

> rm document.pdf
File removed: document.pdf
```

**Features:**
- Removes files from current directory only
- Error handling for non-existent files
- Safety check to prevent directory deletion

---

#### `cp` - Copy
Copies files or directories.

**Syntax:**
```bash
cp <source_file> <destination_file>           # Copy file
cp -r <source_directory> <destination_dir>    # Copy directory recursively
```

**Examples:**
```bash
# Copy file
> cp source.txt destination.txt
File copied successfully

# Copy directory recursively
> cp -r sourceFolder destinationFolder
Directory copied successfully (recursive)

# Copy with paths
> cp /path/to/source.txt ./local-copy.txt
File copied successfully
```

**Features:**
- File-to-file copying
- Recursive directory copying with `-r` flag
- Preserves directory structure
- Overwrites existing files with confirmation

---

#### `cat` - Concatenate and Display
Displays file contents or concatenates multiple files.

**Syntax:**
```bash
cat <file>              # Display one file
cat <file1> <file2>     # Display and concatenate two files
```

**Examples:**
```bash
> cat file.txt
This is the content of file.txt

> cat file1.txt file2.txt
Content of file1.txt

Content of file2.txt
```

**Features:**
- Display single file content
- Concatenate and display two files
- Error handling for missing files

---

#### `wc` - Word Count
Counts lines, words, and characters in a file.

**Syntax:**
```bash
wc <filename>
```

**Examples:**
```bash
> wc document.txt
42 350 2105 document.txt

Explanation:
42 lines, 350 words, 2105 characters (excluding newlines), filename
```

**Features:**
- Line count (number of newlines)
- Word count (whitespace-separated)
- Character count (excluding newlines)
- Matches Unix `wc` format

---

### Compression Operations

#### `zip` - Create ZIP Archive
Compresses files or directories into a ZIP archive.

**Syntax:**
```bash
zip <archive.zip> <file1> [file2] ...           # Compress files
zip -r <archive.zip> <directory>                # Compress directory recursively
```

**Examples:**
```bash
# Compress files
> zip archive.zip file1.txt file2.txt file3.txt
Archive created: /current/path/archive.zip

# Compress directory recursively
> zip -r project-backup.zip project-folder
Archive created: /current/path/project-backup.zip

# Compress with paths
> zip backup.zip /path/to/file1.txt ./local-file.txt
Archive created: /current/path/backup.zip
```

**Features:**
- Compress multiple files
- Recursive directory compression with `-r`
- Preserves directory structure
- Creates ZIP-compatible archives

---

#### `unzip` - Extract ZIP Archive
Extracts files from a ZIP archive.

**Syntax:**
```bash
unzip <archive.zip>                    # Extract to current directory
unzip <archive.zip> -d <destination>   # Extract to specific directory
```

**Examples:**
```bash
# Extract to current directory
> unzip archive.zip
Archive extracted to: /current/directory

# Extract to specific location
> unzip archive.zip -d /path/to/destination
Archive extracted to: /path/to/destination

# Extract with relative path
> unzip backup.zip -d ./restored
Archive extracted to: /current/path/restored
```

**Features:**
- Extract to current directory by default
- Custom destination with `-d` flag
- Creates destination directory if needed
- Preserves original directory structure

---

### I/O Redirection

#### `>` - Output Redirection (Overwrite)
Redirects command output to a file, overwriting existing content.

**Syntax:**
```bash
<command> > <filename>
```

**Examples:**
```bash
> pwd > current-dir.txt
# Creates/overwrites current-dir.txt with pwd output

> ls > file-list.txt
# Creates/overwrites file-list.txt with directory listing
```

---

#### `>>` - Output Redirection (Append)
Redirects command output to a file, appending to existing content.

**Syntax:**
```bash
<command> >> <filename>
```

**Examples:**
```bash
> pwd >> log.txt
# Appends pwd output to log.txt

> ls >> log.txt
# Appends directory listing to log.txt
```

**Features:**
- Works with `pwd` and `ls` commands
- Creates file if it doesn't exist
- `>` overwrites, `>>` appends
- Preserves original output format

---

### System Commands

#### `exit` - Exit CLI
Terminates the command line interpreter.

**Syntax:**
```bash
exit
```

**Example:**
```bash
> exit
[CLI terminates]
```

---

## 🚀 Installation & Usage

### Prerequisites
- **Java Development Kit (JDK)**: Version 17 or higher
- **Operating System**: Windows, macOS, or Linux
- **Terminal/Command Prompt**: For running the CLI

### Compilation

```bash
# Navigate to the project directory
cd path/to/project

# Compile the Java file
javac Terminal.java
```

### Running the CLI

```bash
# Run the Terminal
java Terminal
```

### Example Session

```bash
$ java Terminal
> pwd
Current directory: /home/user

> mkdir testFolder
Directory created: /home/user/testFolder

> cd testFolder
Changed directory to: /home/user/testFolder

> touch file1.txt
File created: file1.txt

> touch file2.txt
File created: file2.txt

> ls
file1.txt
file2.txt

> ls > files.txt

> cat files.txt
file1.txt
file2.txt

> cd ..
Moved up to parent: /home/user

> zip -r backup.zip testFolder
Archive created: /home/user/backup.zip

> exit
```

## 💡 Implementation Details

### Path Handling
- Uses Java NIO `Path` API for robust path operations
- Resolves relative paths against current directory
- Normalizes paths to handle `.` and `..` correctly
- Validates paths before operations

### Error Handling Strategy
1. **Validation**: Check arguments before execution
2. **Try-Catch**: Wrap file operations in exception handlers
3. **User Feedback**: Provide clear, actionable error messages
4. **Graceful Degradation**: Continue running after errors

### File I/O
- Uses `Files` class for modern Java I/O operations
- Buffered reading/writing for efficiency
- Proper resource management with try-with-resources
- Character encoding handling

### Stream Processing
- `DirectoryStream` for efficient directory iteration
- `ZipOutputStream`/`ZipInputStream` for compression
- Stream operations for sorting and filtering

## 🐛 Error Handling

### Common Error Scenarios

1. **Invalid Command**
   ```bash
   > invalidcmd
   Error: Command not found or invalid parameters.
   ```

2. **Wrong Number of Arguments**
   ```bash
   > cd arg1 arg2
   Error: cd takes at most one argument.
   ```

3. **Directory Not Found**
   ```bash
   > cd /nonexistent/path
   Error: Directory does not exist.
   ```

4. **File Not Found**
   ```bash
   > cat missing.txt
   File does not exist: /current/path/missing.txt
   ```

5. **Non-Empty Directory**
   ```bash
   > rmdir folderWithFiles
   Error: Cannot remove non-empty directory: folderWithFiles
   ```

6. **Permission Denied**
   ```bash
   > touch /root/protected.txt
   Error: Access denied
   ```

### Error Handling Features
- ✅ Non-terminating errors (CLI continues running)
- ✅ Descriptive error messages with context
- ✅ Path validation before operations
- ✅ File existence checks
- ✅ Permission verification
- ✅ Type checking (file vs directory)

## 📊 Command Summary Table

| Command | Arguments | Description | Redirection |
|---------|-----------|-------------|-------------|
| `pwd` | None | Print working directory | ✅ |
| `cd` | 0-1 | Change directory | ❌ |
| `ls` | None | List directory contents | ✅ |
| `mkdir` | 1+ | Create directories | ❌ |
| `rmdir` | 1 | Remove empty directories | ❌ |
| `touch` | 1 | Create empty file | ❌ |
| `rm` | 1 | Remove file | ❌ |
| `cp` | 2 | Copy file/directory | ❌ |
| `cat` | 1-2 | Display/concatenate files | ❌ |
| `wc` | 1 | Count lines, words, chars | ❌ |
| `zip` | 2+ | Create ZIP archive | ❌ |
| `unzip` | 1-3 | Extract ZIP archive | ❌ |
| `exit` | None | Terminate CLI | ❌ |

## 🎓 Academic Information

**Assignment Details:**
- **Course**: Operating Systems 1
- **Assignment**: Assignment 1 - Command Line Interpreter
- **Maximum Score**: 24 marks (scaled to 6 marks)
- **Grading Criteria**:
  - Parsing commands: 5 marks
  - Path handling: 4 marks  
  - Command implementation: 15 marks (1 mark each)

**Learning Objectives:**
- Understand command parsing and interpretation
- Practice file system operations
- Implement path resolution (absolute/relative)
- Handle errors gracefully
- Work with I/O streams
- Implement compression algorithms

## 📝 Notes

### Implementation Constraints
- ✅ Must use Java language
- ✅ Cannot modify given class structure
- ✅ Cannot use `exec()` to run system commands
- ✅ Can use Java built-in functions and classes
- ✅ Must handle all specified command cases

### Best Practices Used
- **Modular Design**: Separate methods for each command
- **Helper Methods**: Reusable utilities for common operations
- **Consistent Naming**: Clear, descriptive method names
- **Documentation**: Inline comments for complex logic
- **Resource Management**: Proper closing of streams and files
- **Input Validation**: Check arguments before processing

## 🔍 Testing

### Test Cases

#### Test Case 1: Basic Navigation
```bash
> pwd
Current directory: /home/user

> mkdir test
Directory created: /home/user/test

> cd test
Changed directory to: /home/user/test

> pwd
Current directory: /home/user/test

> cd ..
Moved up to parent: /home/user
```

#### Test Case 2: File Operations
```bash
> touch file1.txt
File created: file1.txt

> touch file2.txt
File created: file2.txt

> ls
file1.txt
file2.txt

> cp file1.txt file1-copy.txt
File copied successfully

> rm file2.txt
File removed: file2.txt

> ls
file1-copy.txt
file1.txt
```

#### Test Case 3: I/O Redirection
```bash
> ls > output.txt
> cat output.txt
file1-copy.txt
file1.txt
output.txt

> pwd >> output.txt
> cat output.txt
file1-copy.txt
file1.txt
output.txt
Current directory: /home/user/test
```

#### Test Case 4: Compression
```bash
> zip -r archive.zip ../test
Archive created: /home/user/archive.zip

> mkdir extracted
Directory created: /home/user/extracted

> unzip archive.zip -d extracted
Archive extracted to: /home/user/extracted
```

## 🤝 Contributing

This is an academic project. If you're a student:
- Use this as a reference for understanding concepts
- Do not copy code for academic submissions
- Implement your own solution based on assignment requirements

## 📄 License

This project is for educational purposes as part of the Operating Systems 1 course at Cairo University.

---

**Institution**: Cairo University - Faculty of Computers & Artificial Intelligence  
**Course**: Operating Systems 1  
**Assignment**: Command Line Interpreter  
**Language**: Java 17+
