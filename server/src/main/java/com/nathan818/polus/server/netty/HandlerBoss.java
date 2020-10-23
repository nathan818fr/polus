package com.nathan818.polus.server.netty;

import com.nathan818.hazel.protocol.event.HazelDisconnectedEvent;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.server.connection.handler.PacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class HandlerBoss extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HandlerBoss.class);

    private final HandlerChannel channel;
    private PacketHandler handler;
    private boolean handlerActive = false;

    public HandlerBoss(HandlerChannel channel, PacketHandler handler) {
        this.channel = channel;
        this.handler = handler;
        handler.setConnection(channel);
    }

    public void setHandler(PacketHandler newHandler) {
        newHandler.setConnection(channel);
        if (!handlerActive) {
            handler = newHandler;
        } else {
            try {
                handler.disabled(false);
            } finally {
                handler = newHandler;
                handler.enabled(false);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handlerActive = true;
        handler.enabled(true);
        logger.info(handler + " has connected from " + channel.getAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channel.markDisconnected();
        handler.disabled(true);
        String reason = channel.getDisconnectReason();
        logger.info(handler + " has disconnected" + (reason != null ? ": " + reason : ""));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        PolusPacket packet = (PolusPacket) msg;
        handler.handlePacket(packet);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof HazelDisconnectedEvent) {
            String disconnectReason = ((HazelDisconnectedEvent) evt).reason();
            channel.setDisconnectReason(disconnectReason);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!ctx.channel().isActive()) {
            return;
        }

        try {
            if (handler.handleException(cause)) {
                return;
            }
        } catch (Exception ex) {
            logger.error(handler + " - exception processing exception", ex);
        }

        // TODO: Conditionally log exceptions
        logger.error(handler + " - encountered exception", cause);

        ctx.close();
    }
}
