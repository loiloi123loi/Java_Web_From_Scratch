package com.polime.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.polime.core.AppConfig;
import com.polime.core.DatabaseManager;
import com.polime.service.UserServiceTest;
import com.polime.utils.JwtUtils;

public class TestRunner {
    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    private static final Map<String, BaseServiceTest> testClasses = new HashMap<>();

    static {
        AppConfig config = new AppConfig();
        String dbUrl = config.getProperty("db.url");
        String dbUsername = config.getProperty("db.username");
        String dbPassword = config.getProperty("db.password", "");
        DatabaseManager.init(dbUrl, dbUsername, dbPassword);

        JwtUtils.init(config.getProperty("jwt.access_token_secret"),
                config.getLongProperty("jwt.access_token_expires_in", 900000L),
                config.getProperty("jwt.refresh_token_secret"),
                config.getLongProperty("jwt.refresh_token_expires_in", 2592000000L),
                config.getProperty("jwt.email_verify_token_secret"),
                config.getLongProperty("jwt.email_verify_token_expires_in", 604800000L));
    }

    public static void registerTest(BaseServiceTest test) {
        String className = test.getClass().getSimpleName();
        testClasses.put(className, test);
    }

    public static void recordPass() {
        totalTests++;
        passedTests++;
    }

    public static void recordFail() {
        totalTests++;
        failedTests++;
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║         RUNNING TESTS                  ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        registerTest(new UserServiceTest());

        if (args.length == 0) {
            runAllTests();
        } else {
            runSelectedTests(args);
        }

        printFinalSummary();
        DatabaseManager.closeConnection();

        if (failedTests > 0) {
            System.exit(1);
        }
    }

    private static void runAllTests() {
        for (BaseServiceTest test : testClasses.values()) {
            test.runAllTests();
            System.out.println();
        }
    }

    private static void runSelectedTests(String[] args) {
        Map<String, Set<String>> classMethodMap = new HashMap<>();

        for (String arg : args) {
            if (arg.contains(".")) {
                String[] parts = arg.split("\\.", 2);
                String className = parts[0];
                String methodName = parts[1];
                classMethodMap.computeIfAbsent(className, k -> new HashSet<>()).add(methodName);
            } else {
                classMethodMap.put(arg, null);
            }
        }

        for (Map.Entry<String, Set<String>> entry : classMethodMap.entrySet()) {
            String className = entry.getKey();
            Set<String> methods = entry.getValue();

            BaseServiceTest test = testClasses.get(className);
            if (test == null) {
                System.out.println("[!] Test class not found: " + className);
                System.out.println("  Available: " + String.join(", ", testClasses.keySet()));
                continue;
            }

            if (methods == null) {
                test.runAllTests();
            } else {
                test.printHeader(className);
                runSpecificMethods(test, methods);
            }
            System.out.println();
        }
    }

    private static void runSpecificMethods(BaseServiceTest test, Set<String> methodNames) {
        Class<?> clazz = test.getClass();
        List<String> notFound = new ArrayList<>();

        for (String methodName : methodNames) {
            try {
                Method method = clazz.getMethod(methodName);
                method.invoke(test);
            } catch (NoSuchMethodException e) {
                notFound.add(methodName);
            } catch (Exception e) {
                System.out.println("  " + methodName + " ... [ERROR]");
                System.out.println("    " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage());
                TestRunner.recordFail();
            }
        }

        if (!notFound.isEmpty()) {
            System.out.println("\n[!] Methods not found: " + String.join(", ", notFound));
            printAvailableMethods(clazz);
        }
    }

    private static void printAvailableMethods(Class<?> clazz) {
        System.out.println("  Available test methods:");
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().startsWith("test") && method.getParameterCount() == 0) {
                System.out.println("    - " + method.getName());
            }
        }
    }

    private static void printFinalSummary() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           SUMMARY                      ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║  Total:  %-30d║%n", totalTests);
        System.out.printf("║  Passed: %-30d║%n", passedTests);
        System.out.printf("║  Failed: %-30d║%n", failedTests);
        System.out.println("╠════════════════════════════════════════╣");
        if (failedTests > 0) {
            System.out.println("║  RESULT: [FAILED] SOME TESTS FAILED!   ║");
        } else if (totalTests == 0) {
            System.out.println("║  RESULT: [!] NO TESTS EXECUTED         ║");
        } else {
            System.out.println("║  RESULT: [PASSED] ALL TESTS PASSED!    ║");
        }
        System.out.println("╚════════════════════════════════════════╝");
    }
}
