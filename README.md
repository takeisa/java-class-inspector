# Class Inspector

Class Inspector is a command-line tool that analyzes JAR files to inspect Java classes, methods, and fields. It uses the ASM library to read Java bytecode and can identify which classes, methods, and fields are referenced or unreferenced within the JAR file.

# クラスインスペクター

クラスインスペクターは、JARファイルを分析してJavaクラス、メソッド、フィールドを検査するコマンドラインツールです。ASMライブラリを使用してJavaバイトコードを読み取り、JARファイル内で参照されているクラス、メソッド、フィールドと参照されていないものを識別できます。

## Features / 機能

- Analyze JAR files to inspect all contained classes / JARファイル内のすべてのクラスを分析
- Display detailed information about classes, including their superclasses / スーパークラスを含むクラスの詳細情報を表示
- List all methods and fields in each class / 各クラスのすべてのメソッドとフィールドを一覧表示
- Mark referenced (✓) and unreferenced (✗) methods and fields / 参照されている(✓)メソッドとフィールドと参照されていない(✗)メソッドとフィールドを表示
- Show which classes reference each method and field / 各メソッドとフィールドを参照しているクラスを表示
- Provide overall statistics about the JAR file contents / JARファイルの内容に関する全体的な統計情報を提供
- Support for analyzing multiple JAR files in a single run / 一度の実行で複数のJARファイルを分析可能

## Installation / インストール

### Prerequisites / 前提条件

- Java 11 or higher / Java 11以上
- Gradle 7.0 or higher (for building from source) / Gradle 7.0以上（ソースからビルドする場合）

### Building from Source / ソースからのビルド

1. Clone the repository / リポジトリをクローン:
   ```
   git clone https://github.com/yourusername/class_inspector.git
   cd class_inspector
   ```

2. Build the project using Gradle / Gradleを使用してプロジェクトをビルド:
   ```
   ./gradlew build
   ```

3. Create a distributable package / 配布可能なパッケージを作成:
   ```
   ./gradlew installDist
   ```

The executable will be available in `build/install/class_inspector/bin/`.
実行ファイルは `build/install/class_inspector/bin/` に作成されます。

Alternatively, you can create a standalone JAR file / または、スタンドアロンのJARファイルを作成することもできます:
```
./gradlew jar
```

The JAR file will be created in `build/libs/class_inspector-1.0-SNAPSHOT.jar`.
JARファイルは `build/libs/class_inspector-1.0-SNAPSHOT.jar` に作成されます。

## Usage / 使用方法

### Basic Usage / 基本的な使い方

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file>
```

### Analyzing Multiple JAR Files / 複数のJARファイルの分析

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file-1> <path-to-jar-file-2> ...
```

### Example Output / 出力例

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

## Understanding the Output / 出力の理解

- **✓** indicates a referenced method or field / 参照されているメソッドまたはフィールドを示します
- **✗** indicates an unreferenced method or field / 参照されていないメソッドまたはフィールドを示します
- For each method and field, the tool shows which classes reference it (if any) / 各メソッドとフィールドについて、それを参照しているクラスを表示します（存在する場合）
- The summary at the end of each class shows how many methods and fields are referenced or unreferenced / 各クラスの最後にある要約は、参照されているメソッドとフィールドの数と参照されていないメソッドとフィールドの数を示します
- The overall statistics at the end show totals for the entire JAR file / 最後の全体統計は、JARファイル全体の合計を示します

## Use Cases / ユースケース

- Identifying dead code (unreferenced methods and fields) / デッドコード（参照されていないメソッドとフィールド）の特定
- Understanding class hierarchies and dependencies / クラス階層と依存関係の理解
- Analyzing third-party libraries / サードパーティライブラリの分析
- Optimizing code by removing unused elements / 未使用の要素を削除してコードを最適化
- Educational purposes to understand Java bytecode / Javaバイトコードを理解するための教育目的

## Dependencies / 依存関係

- [ASM](https://asm.ow2.io/) (9.5) - Java bytecode manipulation and analysis framework / Javaバイトコード操作および分析フレームワーク
- [ASM Commons](https://asm.ow2.io/) (9.5) - Additional utilities for ASM / ASMの追加ユーティリティ

## License / ライセンス

[Add license information here / ここにライセンス情報を追加]

## Contributing / 貢献

[Add contribution guidelines here / ここに貢献ガイドラインを追加]

## Acknowledgments / 謝辞

- The ASM team for their excellent bytecode analysis library / 優れたバイトコード分析ライブラリを提供してくれたASMチーム
