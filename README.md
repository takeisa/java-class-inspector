# Class Inspector

[日本語READMEはこちら](./README_ja.md)

Class Inspector is a command-line tool that analyzes JAR files to inspect Java classes, methods, and fields. It uses the ASM library to read Java bytecode and can identify which classes, methods, and fields are referenced or unreferenced within the JAR file.

## Features

- Analyze JAR files to inspect all contained classes
- Display detailed information about classes, including their superclasses
- List all methods and fields in each class
- Mark referenced (✓) and unreferenced (✗) methods and fields
- Show which classes reference each method and field
- Provide overall statistics about the JAR file contents
- Support for analyzing multiple JAR files in a single run

## Installation

### Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher (for building from source)

### Building from Source

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/class_inspector.git
   cd class_inspector
   ```

2. Build the project using Gradle:
   ```
   ./gradlew build
   ```

3. Create a distributable package:
   ```
   ./gradlew installDist
   ```

The executable will be available in `build/install/class_inspector/bin/`.

Alternatively, you can create a standalone JAR file:
```
./gradlew jar
```

The JAR file will be created in `build/libs/class_inspector-1.0-SNAPSHOT.jar`.

## Usage

### Basic Usage

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file>
```

### Analyzing Multiple JAR Files

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file-1> <path-to-jar-file-2> ...
```

### Example Output

```
Inspecting classes in example.jar:

Class: com.example.MyClass
--------------------------------------------------------------------------------
  Superclass: java.lang.Object
  Methods:
    ✓ main(String[]): void
      Referenced by: com.example.MyClass
    ✗ unusedMethod(int, String): boolean
      Not referenced by any class
  Summary: 1 methods referenced, 1 methods not referenced
  Fields:
    ✓ logger: Logger
      Referenced by: com.example.MyClass
    ✗ unusedField: int
      Not referenced by any class
  Summary: 1 fields referenced, 1 fields not referenced

Overall Statistics:
================================================================================
Total classes: 10
Total methods: 50 (Referenced: 35, Not referenced: 15)
Total fields: 25 (Referenced: 20, Not referenced: 5)
```

## Understanding the Output

- **✓** indicates a referenced method or field
- **✗** indicates an unreferenced method or field
- For each method and field, the tool shows which classes reference it (if any)
- The summary at the end of each class shows how many methods and fields are referenced or unreferenced
- The overall statistics at the end show totals for the entire JAR file

## Use Cases

- Identifying dead code (unreferenced methods and fields)
- Understanding class hierarchies and dependencies
- Analyzing third-party libraries
- Optimizing code by removing unused elements
- Educational purposes to understand Java bytecode

## Dependencies

- [ASM](https://asm.ow2.io/) (9.5) - Java bytecode manipulation and analysis framework
- [ASM Commons](https://asm.ow2.io/) (9.5) - Additional utilities for ASM

## License

This project is licensed under the MIT License.

## Acknowledgments

- The ASM team for their excellent bytecode analysis library
