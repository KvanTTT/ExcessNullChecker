package test;

public class MethodCalls {

    public void callFinal() {
        if (callFinalHelper() != null) { // Test: condition_is_always_true
        }
    }

    public final Object callFinalHelper() {
        return new Object();
    }

    public void callFinal2() {
        if (callFinalHelper2() != null) { // Test: condition_is_always_false
        }
    }

    public final Object callFinalHelper2() {
        return null;
    }

    public void callStatic() {
        if (callStaticHelper() != null) { // Test: condition_is_always_true
        }
    }

    public static Object callStaticHelper() {
        return new Object();
    }

    public void callInstance() {
        if (callInstanceHelper() != null) {
        }
    }

    public Object callInstanceHelper() {
        return new Object();
    }

    static void callWithPassedParam() {
        Object param = new Object();
        if (callWithPassedParamHelper(param) != null) { // Test: condition_is_always_true
        }
    }

    static Object callWithPassedParamHelper(Object param) {
        return param;
    }

    static void callWithPassedParam2(int x) {
        Object param = new Object();
        if (callWithPassedParamHelper2(x, param) != null) {
        }
    }

    static Object callWithPassedParamHelper2(int x, Object param) {
        return x > 5 ? param : null;
    }

    void recursionCall() {
        Object x = recursionHelper(10);
        if (x != null) { // Test: condition_is_always_true
        }
    }

    static Object recursionHelper(int a) {
        if (a >= 5) {
            return recursionHelper(a - 5);
        }

        return new Object();
    }
}