package test;

public class Example4 {
    public final Object foo() {
        return new Object();
    }

    public void test() {
        if (foo() != null) { // Test: condition_is_always_true
            System.out.println("foo() != null");
        }
    }
}