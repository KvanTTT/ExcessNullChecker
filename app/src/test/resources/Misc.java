package test;

public class Misc {
    static void doubleCheckTheSameCondition(Object x) {
        if (x == null) {
            if (x == null) { // Test: condition_is_always_true
            }
        }
    }

    static void logicalAnd(Object a) {
        if (a != null && a == null) { // Test: condition_is_always_false
        }
    }

    static void logicalOr(Object a) {
        if (a != null || a == null) { // Test: condition_is_always_true
        }
    }

    static void unknownCondition(int x) {
        Object a = x > 7 ? new Object() : null;
        if (a == null) { // No warning
        }
    }

    static void assignInBothBranches(int x) {
        Object a = x > 7 ? new Object() : new Object();
        if (a == null) { // Test: condition_is_always_false
        }
    }

    static void useVarInDifferentBlocks(int x) {
        Object b = new Object();
        if (x > 7) {
            b = null;
            if (b == null) { // Test: condition_is_always_true
            }
        }

        if (b == null) { // No warning
        }
    }

    static void mergeStacksOfDifferentSize(Object a) {
        if (a != null) {
            Object b = a;
            if (b != null) { // Test: condition_is_always_true
            }
        }
    }

    static void loop(int param, Object a) {
        for (int i = 0; i < param; i++) {
            a = new Object();
        }
        if (a != null) { // No warning
        }
    }

    static void checkAndReassign(Object a, Object b, Object c) {
        if (a == null) {
            a = new Object();
        }
        if (a == null) { // Test: condition_is_always_false
        }

        if (b != null)
            b = null;
        if (b == null) { // Test: condition_is_always_true
        }

        if (c == null) {
            c = null;
        }
        if (c == null) { // Not always null
        }
    }

    static void checkcast(Object o2) {
        Object o1 = new Object();
        String s = (String)o1;
        if (s == null) { //  Test: condition_is_always_false
        }
        if ((String)o2 == null) { // No warning
        }
    }

    static void dropReturnResult(Object a) {
        dropReturnResultHelper();
    }

    static int dropReturnResultHelper() {
        return 4;
    }
}