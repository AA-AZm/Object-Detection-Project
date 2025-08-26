package Abubaker_Object_Detection.util;

public class Logger {

    public static void startLog() {
        System.out.println("Logging started...");
    }

    public static void logInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void logError(String message, Throwable t) {
        System.err.println("[ERROR] " + message);
        if (t != null) {
            t.printStackTrace();
        }

    }

}
