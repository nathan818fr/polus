package com.nathan818.polus.server.config;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;

@Data
public class Config {
    @NotEmpty
    private List<@NotNull @Valid ListenerConfig> listeners;

    @NotNull
    @Pattern(regexp = "^(?:auto|[1-9][0-9]{0,9})$", message = "must be 'auto' or a strictly positive integer")
    private String networkThreads = "auto";

    @NotNull
    @Pattern(regexp = "^(?:auto|[1-9][0-9]{0,9})$", message = "must be 'auto' or a strictly positive integer")
    private String gameThreads = "auto";

    public int getNetworkThreads(int autoValue) {
        return networkThreads.equals("auto") ? autoValue : Integer.parseInt(networkThreads);
    }

    public int getGameThreads(int autoValue) {
        return gameThreads.equals("auto") ? autoValue : Integer.parseInt(gameThreads);
    }
}
