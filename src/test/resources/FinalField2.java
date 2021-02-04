package test;

public class FinalField2 {
    private final Object x;

    public FinalField2() {
        x = new Object();
    }

    public FinalField2(Object a) {
        x = new Object();
    }

    public void test() {
        if (x == null) { // Test: false
            System.out.println("x == null");
        }
    }
}