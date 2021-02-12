# Excess Null Checker

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
  In this case, all `.class` and `.java` files are being analyzed within the passed directory.

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

Tester uses them to identify if the tool correctly reports expected warnings
and does not report false messages.
