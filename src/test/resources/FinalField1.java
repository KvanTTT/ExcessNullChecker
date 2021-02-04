package test;

public class FinalField1 {
    private final Object x;

    public FinalField1() {
        x = new Object();
    }

    public FinalField1(Object a) {
        x = null;
    }

    public void test() {
        if (x == null) {
            System.out.println("x == null");
        }
    }
}