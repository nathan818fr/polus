package com.nathan818.netty.statefuludp;

import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.FixedRecvByteBufAllocator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class StatefulUdpChannelConfig extends DefaultChannelConfig {
    private final Map<ChannelOption<?>, Object> options = new HashMap<>();

    public StatefulUdpChannelConfig(StatefulUdpServerChannel channel) {
        super(channel, new FixedRecvByteBufAllocator(2048));
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        Map<ChannelOption<?>, Object> result = new IdentityHashMap<>();
        result.putAll(options);
        result.putAll(super.getOptions());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        T ret = (T) options.get(option);
        if (ret == null) {
            ret = super.getOption(option);
        }
        return ret;
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        options.put(option, value);
        return true;
    }
}
