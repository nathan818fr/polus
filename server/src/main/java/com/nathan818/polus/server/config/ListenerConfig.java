package com.nathan818.polus.server.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ListenerConfig {
    @NotBlank
    private String ip = "0.0.0.0";

    @Min(0) @Max(65535)
    private int port = 22023;
}
