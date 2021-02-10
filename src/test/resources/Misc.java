package test;

public class Misc {
    static void doubleCheckTheSameCondition(Object x) {
        if (x == null) {
            if (x == null) { // Test: condition_is_always_true
            }
        }
    }

    static void unknownCondition(int x) {
        Object a = x > 7 ? new Object() : null;
        if (a == null) { // No warning
        }
    }

    static void loop(int param, Object a) {
        for (int i = 0; i < param; i++) {
            a = new Object();
        }
        if (a != null) { // No warning
        }
    }

    static void array() {
        String[] a = new String[0];
        if (a != null) { // Test: condition_is_always_true
        }
    }
}