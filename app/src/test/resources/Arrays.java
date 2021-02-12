package test;

public class Arrays {
    static void arrayInitialization() {
        String[] a = new String[0];
        if (a != null) { // Test: condition_is_always_true
        }

        int[][][] b = new int[1][2][3];
        if (b != null) { // Test: condition_is_always_true
        }

        int[] c = new int[5];
        if (c != null) { // Test: condition_is_always_true
        }
    }

    static void arrayLengthStoreAndLoad(Object[] z, int[] x, int[] y) {
        int l = x.length;
        if (x != null) { // Test: condition_is_always_true
        }

        int a = x[3];
        if (x != null) { // Test: condition_is_always_true
        }

        y[1] = 34;
        if (y != null) { // Test: condition_is_always_true
        }
    }
}