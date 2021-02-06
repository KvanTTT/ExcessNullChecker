package test;

public class Example6 {
    public void test(Object a) {
        if (a != null) {
            if (a != null) { // Test: condition_is_always_true
                System.out.println("a != null");
            }
        }
    }

    public void test2(Object a, Object b) {
        if (a != null) {
            System.out.println("a != null");
            return;
        }

        if (b != null) {
            if (b != null) { // Test: condition_is_always_true
                System.out.println("b != null");
            }
        }
    }

    public static void test3(Object x)
    {
        if (x == null) System.out.println("x == null");
        if (x == null) System.out.println("x == null");
    }

    public void test4(Object a, Object b) {
        if (a != null) {
            if (b == null) {
                return;
            }
            if (b != null) { // Test: condition_is_always_true
                System.out.println("b != null");
            }
        }

        if (b != null) {
            System.out.println("b != null");
        }
    }

    public void test5(Object a, Object b) {
        if (a == null) {
            if (b == null) {
                return;
            }
        }

        if (b == null) {
            if (a != null) { // Test: condition_is_always_true
                System.out.println("a != null && b == null");
            }
        }
    }

    public void test6(Object a, Object b, Object c) {
        if (a == null) {
            if (b == null) {
                return;
            }
        }

        if (b == null) {
            if (c == null) {
                return;
            }
        }

        if (c == null) {
            if (a == null) {
                System.out.println("c == null && a == null");
            }
        }

        if (c == null) {
            if (b == null) { // Test: condition_is_always_false
                System.out.println("c == null && b == null");
            }
        }
    }

    public void test7(Object a) {
        if (a == null) {
            if (a == null) { // Test: condition_is_always_true
                System.out.println("a == null");
            }

            if (a == null) { // Test: condition_is_always_true
                System.out.println("a == null");
            }
        }
    }
}