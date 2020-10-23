package com.nathan818.netty.statefuludp.util;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroupException;
import java.util.Iterator;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DefaultChannelGroupFutures {
    public static Throwable flattenException(Throwable cause) {
        if (cause instanceof ChannelGroupException) {
            Iterator<Map.Entry<Channel, Throwable>> it = ((ChannelGroupException) cause).iterator();
            if (it.hasNext()) {
                cause = it.next().getValue();
                while (it.hasNext()) {
                    cause.addSuppressed(it.next().getValue());
                }
            }
        }
        return cause;
    }
}
