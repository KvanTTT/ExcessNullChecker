package test;

public class SwitchCase {
    static void lookupSwitchCase1(int x) {
        Object a = null;
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

    static void lookupSwitchCase2(int x) {
        Object a = null;
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

    static void lookupSwitchCase3(int x) {
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

    static void lookupSwitchCase4(int x) {
        Object a = new Object();
        switch (x) {
            default:
                a = null;
                break;
        }
        if (a != null) { // Test: condition_is_always_false
        }
    }

    static void tableSwitchCase1(int x) {
        Object a = null;
        switch (x) {
            case 0:
                a = "string0";
                break;
            case 1:
                a = "string1";
                break;
            case 2:
                a = "string2";
                break;
            default:
                a = "string3";
                break;
        }
        if (a != null) { // Test: condition_is_always_true
        }
    }

    static void tableSwitchCase2(int x) {
        Object a = null;
        switch (x) {
            case 0:
                a = "string0";
                break;
            case 1:
                a = "string1";
                break;
            case 2:
                a = "string2";
                break;
            default:
                a = null;
                break;
        }
        if (a != null) {
        }
    }

    static void tableSwitchCase3(int x) {
        Object a = null;
        switch (x) {
            case 0:
                a = "string0";
                break;
            case 1:
                a = "string1";
                break;
            case 4:
                a = "string2";
                break;
        }
        if (a != null) {
        }
    }
}