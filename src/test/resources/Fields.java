package test;

public class Fields {
    private Object x = null;
    private final Object y1 = new Object();
    private final Object y2 = null;
    private static Object z = null;
    private final static Object w1 = new Object();
    private final static Object w2;
    private final Object a;
    private final Object b;
    private final Object c = w2;

    static {
        w2 = new Object();
    }

    public Fields() {
        a = new Object();
        b = new Object();
    }

    public Fields(int param) {
        a = new Object();
        b = null;
    }

    public void test() {
        if (x == null) { // No warning since field is not final
        }
        if (y1 == null) { // Test: false
        }
        if (y2 == null) { // Test: true
        }
        if (z == null) { // No warning since field is not final
        }
        if (w1 == null) { // Test: false
        }

        if (a == null) { // Test: false
        }
        if (b == null) { // No warning since b may be both null and not null
        }
        if (c == null) { // Test: false (IDEA fails here)
        }
    }
}