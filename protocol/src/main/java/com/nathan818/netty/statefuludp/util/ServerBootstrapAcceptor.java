package com.nathan818.netty.statefuludp.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.util.AttributeKey;
import java.lang.reflect.Field;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerBootstrapAcceptor extends ChannelInboundHandlerAdapter {
    private static final String ACCEPTOR_ID = "ServerBootstrap$ServerBootstrapAcceptor#0";

    public static void copy(ServerChannel srcChannel, Channel dstChannel) {
        Object srcHandler = srcChannel.pipeline().get(ACCEPTOR_ID);
        EventLoopGroup childGroup = get(srcHandler, "childGroup");
        ChannelHandler childHandler = get(srcHandler, "childHandler");
        Map.Entry<ChannelOption<?>, Object>[] childOptions = get(srcHandler, "childOptions");
        Map.Entry<AttributeKey<?>, Object>[] childAttrs = get(srcHandler, "childAttrs");

        if (childGroup == dstChannel.eventLoop().parent()) {
            // if group and childGroup are identical, we keep the current channel eventLoop for better performances
            childGroup = dstChannel.eventLoop();
        }

        dstChannel.pipeline().addLast(ACCEPTOR_ID, new ServerBootstrapAcceptor(
                dstChannel,
                childGroup,
                childHandler,
                childOptions,
                childAttrs
        ));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private static <T> T get(Object obj, String fieldName) {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    private final EventLoopGroup childGroup;
    private final ChannelHandler childHandler;
    private final Map.Entry<ChannelOption<?>, Object>[] childOptions;
    private final Map.Entry<AttributeKey<?>, Object>[] childAttrs;

    public ServerBootstrapAcceptor(final Channel channel, EventLoopGroup childGroup, ChannelHandler childHandler,
            Map.Entry<ChannelOption<?>, Object>[] childOptions, Map.Entry<AttributeKey<?>, Object>[] childAttrs) {
        this.childGroup = childGroup;
        this.childHandler = childHandler;
        this.childOptions = childOptions;
        this.childAttrs = childAttrs;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final Channel child = (Channel) msg;
        child.pipeline().addLast(childHandler);
        setChannelOptions(child, childOptions);
        setAttributes(child, childAttrs);

        try {
            childGroup.register(child)
                    .addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            forceClose(child, future.cause());
                        }
                    });
        } catch (Throwable var5) {
            forceClose(child, var5);
        }
    }

    private static void forceClose(Channel child, Throwable t) {
        child.unsafe().closeForcibly();
        log.warn("Failed to register an accepted channel: {}", child, t);
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private static void setChannelOptions(Channel channel, Map.Entry<ChannelOption<?>, Object>[] options) {
        for (int i = 0, l = options.length; i < l; ++i) {
            Map.Entry<ChannelOption<?>, Object> e = options[i];
            setChannelOption(channel, e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static void setChannelOption(Channel channel, ChannelOption<?> option, Object value) {
        try {
            if (!channel.config().setOption((ChannelOption) option, value)) {
                log.warn("Unknown channel option '{}' for channel '{}'", option, channel);
            }
        } catch (Throwable t) {
            log.warn("Failed to set channel option '{}' with value '{}' for channel '{}'",
                    option, value, channel, t);
        }
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach", "unchecked", "rawtypes"})
    private static void setAttributes(Channel channel, Map.Entry<AttributeKey<?>, Object>[] attrs) {
        for (int i = 0, l = attrs.length; i < l; ++i) {
            Map.Entry<AttributeKey<?>, Object> e = attrs[i];
            AttributeKey<Object> key = (AttributeKey) e.getKey();
            channel.attr(key).set(e.getValue());
        }
    }
}
