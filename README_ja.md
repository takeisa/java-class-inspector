# クラスインスペクター

クラスインスペクターは、JARファイルを分析してJavaクラス、メソッド、フィールドを検査するコマンドラインツールです。ASMライブラリを使用してJavaバイトコードを読み取り、JARファイル内で参照されているクラス、メソッド、フィールドと参照されていないものを識別できます。

## 機能

- JARファイル内のすべてのクラスを分析
- スーパークラスを含むクラスの詳細情報を表示
- 各クラスのすべてのメソッドとフィールドを一覧表示
- 参照されている(✓)メソッドとフィールドと参照されていない(✗)メソッドとフィールドを表示
- 各メソッドとフィールドを参照しているクラスを表示
- JARファイルの内容に関する全体的な統計情報を提供
- 一度の実行で複数のJARファイルを分析可能

## インストール

### 前提条件

- Java 11以上
- Gradle 7.0以上（ソースからビルドする場合）

### ソースからのビルド

1. リポジトリをクローン:
   ```
   git clone https://github.com/yourusername/class_inspector.git
   cd class_inspector
   ```

2. Gradleを使用してプロジェクトをビルド:
   ```
   ./gradlew build
   ```

3. 配布可能なパッケージを作成:
   ```
   ./gradlew installDist
   ```

実行ファイルは `build/install/class_inspector/bin/` に作成されます。

または、スタンドアロンのJARファイルを作成することもできます:
```
./gradlew jar
```

JARファイルは `build/libs/class_inspector-1.0-SNAPSHOT.jar` に作成されます。

## 使用方法

### 基本的な使い方

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file>
```

### 複数のJARファイルの分析

```
java -jar class_inspector-1.0-SNAPSHOT.jar <path-to-jar-file-1> <path-to-jar-file-2> ...
```

### 出力例

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

## 出力の理解

- **✓** 参照されているメソッドまたはフィールドを示します
- **✗** 参照されていないメソッドまたはフィールドを示します
- 各メソッドとフィールドについて、それを参照しているクラスを表示します（存在する場合）
- 各クラスの最後にある要約は、参照されているメソッドとフィールドの数と参照されていないメソッドとフィールドの数を示します
- 最後の全体統計は、JARファイル全体の合計を示します

## ユースケース

- デッドコード（参照されていないメソッドとフィールド）の特定
- クラス階層と依存関係の理解
- サードパーティライブラリの分析
- 未使用の要素を削除してコードを最適化
- Javaバイトコードを理解するための教育目的

## 依存関係

- [ASM](https://asm.ow2.io/) (9.5) - Javaバイトコード操作および分析フレームワーク
- [ASM Commons](https://asm.ow2.io/) (9.5) - ASMの追加ユーティリティ

## ライセンス

このプロジェクトはMITライセンスの下で提供されています。

## 謝辞

- 優れたバイトコード分析ライブラリを提供してくれたASMチーム