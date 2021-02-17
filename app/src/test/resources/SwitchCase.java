package test;

public class SwitchCase {
    static void switchCase1(int x) {
        Object a = new Object();
        switch (x) {
            case 10:
                a = "string0";
                break;
            case 20:
                a = "string1";
                break;
            default:
                a = "string1";
                break;
        }
        if (a != null) { // Test: condition_is_always_true
        }
    }

    static void switchCase2(int x) {
        Object a = new Object();
        switch (x) {
            case 10:
                a = "string0";
                break;
            case 20:
                a = "string1";
                break;
            default:
                a = null;
                break;
        }
        if (a != null) {
        }
    }

    static void switchCase3(int x) {
        Object a = new Object();
        switch (x) {
            case 10:
                a = "string0";
                break;
            default:
                a = null;
                break;
        }
        if (a != null) {
        }
    }

    static void switchCase4(int x) {
        Object a = new Object();
        switch (x) {
            default:
                a = null;
                break;
        }
        if (a != null) { // Test: condition_is_always_false
        }
    }
}