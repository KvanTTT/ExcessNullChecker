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
}