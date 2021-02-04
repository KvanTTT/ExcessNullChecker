package test;

public class FinalField3 {
    private final Object x;

    public FinalField3() {
        x = null;
    }

    public void test() {
        if (x == null) { // Test: true (IDEA fails here!)
            System.out.println("x == null");
        }
    }
}