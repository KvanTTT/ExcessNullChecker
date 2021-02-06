package test;

public class Example1 {
    public static void test1() {
        Object x = null;
        if (x == null) { // Test: condition_is_always_true
            System.out.println("x is null");
        }
    }

    public static void test2() {
        Object x = new Object();
        if (x == null) { // Test: condition_is_always_false
            System.out.println("x is null");
        }
    }

    public static void test3() {
        Object x = "Hello, world!";
        if (x == null) { // Test: condition_is_always_false
            System.out.println("x is null");
        }
    }
}