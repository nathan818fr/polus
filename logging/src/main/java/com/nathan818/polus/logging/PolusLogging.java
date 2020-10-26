package com.nathan818.polus.logging;

import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.LogManager;

@UtilityClass
public class PolusLogging {
    public static void init() {
        if (System.getProperty("log4j2.contextSelector") != null) {
            return;
        }

        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        String loggerContext = LogManager.getContext().getClass().getSimpleName();
        if (!loggerContext.toLowerCase(Locale.ROOT).contains("async")) {
            LogManager.getLogger().warn("Async logger is not enabled (current logger: " + loggerContext + ")");
        }
    }

    public static void shutdown() {
        try {
            LogManager.shutdown();
        } catch (Exception ignored) {
        }
    }
}
