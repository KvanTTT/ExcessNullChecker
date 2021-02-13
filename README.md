# Excess Null Checker

![CI](https://github.com/KvanTTT/ExcessNullChecker/workflows/CI/badge.svg)

## Description

A tool that searches `.java` or `.class` files to detect excess null checks like
the following:

```java
Object x = null;
if (x == null) { // Condition is always true
    System.out.println("x is null");
}
```

```
System.out.println(x.hashCode());
if (x == null) { // Condition is always false
    System.out.println("x == null");
}
```

## Dependencies

* [Gradle build tool](https://gradle.org/)
* [ASM](https://asm.ow2.io/)

## Run

```
gradle run --args='<fileNameOrDirectory>'
```

`<fileNameOrDirectory>` should be
* `.class` file
* `.java`. In this case, `javac` tool is used to obtain `.class` files
* `<fileNameOrDirectory>` (limited functionality).
  In this case, all `.class` and `.java` files are analyzed within the passed directory.

## Test

```
gradle test
```

Test files are located in [`app/src/test/resources`](app/src/test/resources) directory.

Expected warnings placed directly to test files and
marked with the following markers:

```java
// Test: (condition_is_always_true|condition_is_always_false)
```

The tester uses them to identify if the tool correctly reports expected warnings
and does not report false messages.


## How it works

Full workflow located in `Analyzer`

1. In the beginning, it collects declarations (fields) for further use (`DeclCollector`).
2. Secondly it builds [Control-flow graph](https://en.wikipedia.org/wiki/Control-flow_graph)
   that is used to detect points where state
   should be forked or merged. These points are linked with opcode instructions
   and being created on conditional and unconditional jumps.
   On the first code bypass CFG nodes are being created (`CfgNodeCreator`),
   on the second (`CfgNodeInitializer`) they are being linked with each other.
3. After CFG nodes are initialized, it bypasses methods in the following order and
   tries to find excess null checks:
    1. Static constructor
    2. Ordinary constructors
    3. Ordinary methods

   It interprets every instruction and changes the state of the current stack.
   On every joint point obtained from CFG it forks or merges corresponding states.
   On every `IFNULL` or `IFNONNULL` it analyzes the `DataEntry` on the top of the stack
   and reports corresponding warning if it is determined (`Null` or `NotNull`)

The state contains the stack that stores a set of local variables and fields represented by `DataEntry`.
`DataEntry` has the following properties:

* `name`. It can be `<Uninitialized>`, `<Dirty>`,
  another string for a field, and an integer value for a local variable.
* `type`. It can be `Uninitialized`, `Other`, `Null`, `NotNull`.

During the merging procedure the following considerations are used:

* If the first item is `Uninitialized` then the second item is returned
* If the first item equals the second item then the first item is returned
* Otherwise `<Dirty>` or `Other` value is returned.

Also, some optimization is used during resolving CFG nodes (see tests).

