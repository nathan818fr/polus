package com.nathan818.hazel.protocol.event;

import com.nathan818.hazel.protocol.HazelEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class HazelDisconnectedEvent extends HazelEvent {
    private final String reason;
}
