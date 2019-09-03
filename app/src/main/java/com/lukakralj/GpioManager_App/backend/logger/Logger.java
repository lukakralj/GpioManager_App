package com.lukakralj.GpioManager_App.backend.logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * This class unifies the output of the debug statements.
 *
 * @author Luka Kralj
 * @version 1.0
 */
public class Logger {
    private static boolean consoleOutput;
    private static boolean autoFlush;

    private static StringBuffer buffer;
    private static boolean started;

    /**
     * This needs to be called in the main method and should be called only once.
     * This ensures that each run is saving logs into a unique file.
     *
     * @param consoleOutputIn True if we want to output to the console, false if not.
     * @param autoFlushIn True if you want all the logs to be immediately outputed, false if not.
     */
    public static void startLogger(boolean consoleOutputIn, boolean autoFlushIn) {
        if (started) {
            return;
        }
        started = true;
        consoleOutput = consoleOutputIn;
        autoFlush = autoFlushIn;
        buffer = new StringBuffer();

        log("Logger started.");
    }

    /**
     * @see #startLogger(boolean, boolean)
     * By default the logger will be outputting to the console but not into a file.
     */
    public static void startLogger() {
        startLogger(true, true);
    }

    /**
     * Formats the new message and saves it to the buffer.
     * Call flush() if you want to show the buffered messages.
     *
     * @param message Message that we want to log.
     * @param level The importance of the message.
     */
    public static void log(String message, Level level) {
        if (!started) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(new Timestamp(System.currentTimeMillis()));
        buffer.append(timestamp).append(" ");

        switch (level) {
            case INFO: buffer.append("[INFO]"); break;
            case WARNING: buffer.append("[WARNING]"); break;
            case ERROR: buffer.append("[ERROR]"); break;
            case DEBUG: buffer.append("[DEBUG]"); break;
            default: buffer.append("[unknown]"); break;
        }

        buffer.append(" ").append(message);
        if (autoFlush) {
            flush();
        }
        else {
            buffer.append("\n");
        }
    }

    /**
     * Level of this message is INFO.
     *
     * @param message Message that we want to log.
     */
    public static void log(String message) {
        log(message, Level.INFO);
    }

    /**
     * Displays the buffered messages into either the console or a file, depends on the
     * flags set at the beginning of the program.
     */
    public static void flush() {
        if (!started) {
            return;
        }

        if (consoleOutput) {
            System.out.println(buffer);
        }
        buffer = new StringBuffer();
    }
}
