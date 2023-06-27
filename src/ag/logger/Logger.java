package ag.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Logger {

    private static final Map<String, Logger> loggers = new HashMap<>();
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("(yMMdd H:m:s:S Z)");
    public static final String DEFAULT_LOGGER_NAME = "DEFAULT_LOGGER";

    private BufferedWriter writer;
    private Level level = Level.INFO;

    public enum Level {
        DEBUG, INFO, WARNING, ERROR, FATAL, NONE;
    }

    private Logger(OutputStream out) {
        writer = new BufferedWriter(new OutputStreamWriter(out));
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public static Logger getDefaultLogger() {
        Logger logger = getLogger(DEFAULT_LOGGER_NAME);
        if (null == logger) {
            logger = createAndAddLogger(DEFAULT_LOGGER_NAME, System.out);
        }
        return logger;
    }

    public static Logger addLogger(String name, OutputStream out) {
        loggers.computeIfAbsent(name, k -> createAndAddLogger(name, out));
        return loggers.get(name);
    }

    private static Logger createAndAddLogger(String name, OutputStream out) {
        Logger logger = new Logger(out);
        loggers.put(name, logger);
        return logger;
    }


    public static Logger getLogger(String name) {
        return loggers.get(name);
    }

    private String getTimestamp() {
        Calendar timestamp = Calendar.getInstance();
        return timeFormat.format(timestamp.getTime());
    }

    public void log(Level level, String message) {
        if (null == writer || level.ordinal() < this.level.ordinal()) {
            return;
        }
        try {
            writer.write(level.name() + " " + getTimestamp() + ": " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Failed to write to log: " + e.getMessage());
        }
    }

    public void debug(String message) { log(Level.DEBUG, message); }
    public void info(String message) { log(Level.INFO, message); }
    public void warn(String message) { log(Level.WARNING, message); }
    public void error(String message) { log(Level.ERROR, message); }
    public void fatal(String message) { log(Level.FATAL, message); }

    public void setLevel(Level level) {
        this.level = level;
    }
}
