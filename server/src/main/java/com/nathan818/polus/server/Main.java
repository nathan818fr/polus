package com.nathan818.polus.server;

import com.nathan818.polus.logging.PolusLogging;
import io.netty.util.ResourceLeakDetector;
import java.util.Arrays;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {
    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    public static void main(String[] args) throws Exception {
        PolusLogging.init();

        System.setProperty("io.netty.selectorAutoRebuildThreshold", "0"); // https://github.com/netty/netty/issues/2174
        if (System.getProperty("io.netty.leakDetectionLevel") == null) {
            // Disable leak detector by default (instead of using SIMPLE)
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.acceptsAll(Arrays.asList("help"), "Display this help and exit");
        parser.acceptsAll(Arrays.asList("v", "version"), "Print version and exit");

        OptionSet options = parser.parse(args);

        if (options.has("help")) {
            parser.printHelpOn(System.out);
            return;
        }
        if (options.has("version")) {
            System.out.println(PolusServer.class.getPackage().getImplementationVersion());
            return;
        }

        PolusServer server = new PolusServer();
        server.start();
    }
}
