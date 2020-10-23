package com.nathan818.polus.server.netty;

import com.google.common.base.Preconditions;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.connection.HandlerConnection;
import com.nathan818.polus.server.connection.handler.PacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.Getter;

@Getter
public class HandlerChannel extends HandlerConnection implements PolusPacket.Recipient {
    private static InetSocketAddress asInetSocketAddress(SocketAddress addr) {
        if (addr instanceof InetSocketAddress) {
            return (InetSocketAddress) addr;
        }
        return new InetSocketAddress("127.0.0.1", 0);
    }

    private final Channel handle;
    private final EventLoopExecutor executor;

    public HandlerChannel(PolusServer server, Channel handle) {
        super(server, asInetSocketAddress(handle.remoteAddress()));
        this.handle = handle;
        executor = new EventLoopExecutor(handle.eventLoop());
    }

    @Override
    public boolean isServerChannel() {
        return true;
    }

    @Override
    public void setHandler(PacketHandler handler) {
        Preconditions.checkState(getExecutor().inExecutor());
        try {
            handle.pipeline().get(HandlerBoss.class).setHandler(handler);
        } catch (Throwable err) {
            handle.pipeline().fireExceptionCaught(err).close();
        }
    }

    @Override
    protected void send0(PolusPacket packet) {
        handle.writeAndFlush(packet, handle.voidPromise());
    }

    @Override
    protected void disconnect0(Throwable cause) {
        handle.pipeline().fireExceptionCaught(cause).close();
    }

    @Override
    protected void disconnect0(PolusPacket packet) {
        if (packet != null && handle.isActive()) {
            handle.writeAndFlush(packet)
                    .addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, ChannelFutureListener.CLOSE);
        } else {
            handle.flush().close();
        }
    }
}
