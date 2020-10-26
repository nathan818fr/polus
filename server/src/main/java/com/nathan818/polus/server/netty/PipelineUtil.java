package com.nathan818.polus.server.netty;

import com.nathan818.hazel.protocol.HazelServerChildChannel;
import com.nathan818.polus.protocol.PolusDecoder;
import com.nathan818.polus.protocol.PolusEncoder;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.connection.handler.InitialHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.PlatformDependent;
import java.util.concurrent.ThreadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PipelineUtil {
    private static final boolean DEBUG_PROTOCOL_HAZEL = Boolean.parseBoolean(System.getProperty("com.nathan818.polus.server.debugProtocolHazel", "false"));
    private static final boolean DEBUG_PROTOCOL_POLUS = Boolean.parseBoolean(System.getProperty("com.nathan818.polus.server.debugProtocolPolus", "false"));

    public static final String PACKET_DECODER = "polus-packet-decoder";
    public static final String PACKET_ENCODER = "polus-packet-encoder";
    public static final String HANDLER_BOSS = "polus-handler-boss";
    private static boolean EPOLL;

    static {
        if (Boolean.parseBoolean(System.getProperty("com.nathan818.polus.server.epoll", "true"))) {
            if (PlatformDependent.isWindows()) {
                log.info("Epoll is not available on Windows, network performance may be impacted");
            } else {
                EPOLL = Epoll.isAvailable();
                if (!EPOLL) {
                    log.warn("Epoll is not working, network performance may be impacted", Epoll.unavailabilityCause());
                }
            }
        }
    }

    public static boolean isEpoll() {
        return EPOLL;
    }

    public static EventLoopGroup newEventLoopGroup(int threads, ThreadFactory factory) {
        if (EPOLL) {
            return new EpollEventLoopGroup(threads, factory);
        }
        return new NioEventLoopGroup(threads, factory);
    }

    public static Class<? extends DatagramChannel> getDatagramChannel() {
        if (EPOLL) {
            return EpollDatagramChannel.class;
        }
        return NioDatagramChannel.class;
    }

    @RequiredArgsConstructor
    public static class ChildInitializer extends ChannelInitializer<HazelServerChildChannel> {
        private final PolusServer server;

        @Override
        protected void initChannel(HazelServerChildChannel ch) {
            HandlerChannel channel = new HandlerChannel(server, ch);
            HandlerBoss handlerBoss = new HandlerBoss(channel, new InitialHandler());

            ch.pipeline().addLast(PACKET_DECODER, new PolusDecoder(channel));
            ch.pipeline().addLast(PACKET_ENCODER, new PolusEncoder(channel));
            ch.pipeline().addLast(HANDLER_BOSS, handlerBoss);

            if (DEBUG_PROTOCOL_HAZEL) {
                PipelineDebug.withHazelDebug(ch.pipeline(), channel.toString() + ' ');
            }
            if (DEBUG_PROTOCOL_POLUS) {
                PipelineDebug.withPolusDebug(ch.pipeline(), channel.toString() + ' ');
            }
        }
    }
}
