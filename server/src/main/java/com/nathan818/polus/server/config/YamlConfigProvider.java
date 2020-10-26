package com.nathan818.polus.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

@UtilityClass
public class YamlConfigProvider {
    private static final String HEADER = "# This is the main configuration file for Polus.\n"
            + "# For a reference for any variable inside this file, check out the Polus\n"
            + "# README: https://github.com/nathan818fr/polus\n\n";

    private static Yaml createYaml() {
        DumperOptions dumpOpts = new DumperOptions();
        dumpOpts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumpOpts.setPrettyFlow(true);
        LoaderOptions loadOpts = new LoaderOptions();
        loadOpts.setAllowDuplicateKeys(false);
        return new Yaml(new Constructor(Config.class), new Representer(), dumpOpts, loadOpts);
    }

    public static Config readConfig(InputStream src) {
        return createYaml().loadAs(new InputStreamReader(src, StandardCharsets.UTF_8), Config.class);
    }

    public static void writeConfig(OutputStream dst, Config config) throws IOException {
        String dump = HEADER + createYaml().dumpAsMap(config);

        try (Writer wr = new OutputStreamWriter(dst, StandardCharsets.UTF_8)) {
            wr.write(dump);
        }
    }
}
