package com.nathan818.polus.server.config;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Config {
    @NotEmpty
    private List<@NotNull @Valid ListenerConfig> listeners;
}
