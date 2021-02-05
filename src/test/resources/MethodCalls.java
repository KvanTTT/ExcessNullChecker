package test;

public class MethodCalls {

    public void callFinal() {
        if (callFinalHelper() != null) { // Test: true
        }
    }

    public final Object callFinalHelper() {
        return new Object();
    }

    public void callFinal2() {
        if (callFinalHelper2() != null) { // Test: false
        }
    }

    public final Object callFinalHelper2() {
        return null;
    }

    public void callStatic() {
        if (callStaticHelper() != null) { // Test: true
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