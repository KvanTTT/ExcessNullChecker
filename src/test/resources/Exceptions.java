package test;

public class Exceptions {
    static void NotReachableAfterThrow(Object a) throws Exception {
        if (a == null)
            throw new Exception();
        if (a != null) { // Test: condition_is_always_true
        }
    }
}