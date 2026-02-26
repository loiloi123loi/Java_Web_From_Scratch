package com.polime.test;

public abstract class BaseServiceTest {
    protected abstract void setUp() throws Exception;

    protected abstract void tearDown() throws Exception;

    public abstract void runAllTests();

    protected void runTest(String testName, ThrowingRunnable test) {
        System.out.print("  " + testName + " ... ");
        try {
            setUp();
            test.run();
            TestRunner.recordPass();
            System.out.println("[OK] PASSED");
        } catch (AssertionError e) {
            TestRunner.recordFail();
            System.out.println("[FAILED] " + getTestLocation(e));
            System.out.println("    Reason: " + e.getMessage());
        } catch (Throwable e) {
            TestRunner.recordFail();
            System.out.println("[ERROR] " + getTestLocation(e));
            System.out.println("    " + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            try {
                tearDown();
            } catch (Exception e) {
                System.err.println("    Teardown failed: " + e.getMessage());
            }
        }
    }

    private String getTestLocation(Throwable e) {
        String testClassName = this.getClass().getName();
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.getClassName().equals(testClassName)) {
                return "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            }
        }
        return "";
    }

    protected void printHeader(String title) {
        System.out.println("┌────────────────────────────────────────┐");
        System.out.printf("│ %-38s │%n", title);
        System.out.println("└────────────────────────────────────────┘");
    }

    protected static void assertEquals(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("\n      Expected: " + expected + "\n      Actual:   " + actual);
        }
    }

    protected static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + "\n      Expected: " + expected + "\n      Actual:   " + actual);
        }
    }

    protected static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("\n      Expected: non-null\n      Actual:   null");
        }
    }

    protected static void assertNotNull(String message, Object obj) {
        if (obj == null) {
            throw new AssertionError(message + "\n      Expected: non-null\n      Actual:   null");
        }
    }

    protected static void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("\n      Expected: null\n      Actual:   " + obj);
        }
    }

    protected static void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("\n      Expected: true\n      Actual:   false");
        }
    }

    protected static void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new AssertionError(message + "\n      Expected: true\n      Actual:   false");
        }
    }

    protected static void assertFalse(boolean condition) {
        if (condition) {
            throw new AssertionError("\n      Expected: false\n      Actual:   true");
        }
    }

    protected static void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new AssertionError(message + "\n      Expected: false\n      Actual:   true");
        }
    }

    protected static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingRunnable runnable) {
        try {
            runnable.run();
            throw new AssertionError(
                    "\n      Expected: " + expectedType.getSimpleName() + " thrown\n      Actual:   nothing thrown");
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return expectedType.cast(actual);
            }
            throw new AssertionError("\n      Expected: " + expectedType.getSimpleName() + "\n      Actual:   "
                    + actual.getClass().getSimpleName() + ": " + actual.getMessage());
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
