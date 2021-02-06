package test;

public class Example5 {
    private static final String MESSAGE = "Hello, world!";

    public void test() {
        if (MESSAGE != null) { // Test: condition_is_always_true
            System.out.println(MESSAGE);
        }
    }
}