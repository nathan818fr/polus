package com.nathan818.netty.statefuludp;

import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultChannelId;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StatefulUdpServerChildChannel<I> extends AbstractChannel {
    private static final Logger logger = LoggerFactory.getLogger(StatefulUdpServerChildChannel.class);

    private final I udpId;
    private final ChannelMetadata metadata = new ChannelMetadata(false);
    private final DefaultChannelConfig config = new DefaultChannelConfig(this);

    private final AtomicBoolean shouldRegister = new AtomicBoolean(true);
    private Channel ioChannel;

    private volatile boolean active = true;

    public StatefulUdpServerChildChannel(StatefulUdpServerChannel parent, I udpId) {
        this(parent, udpId, DefaultChannelId.newInstance());
    }

    public StatefulUdpServerChildChannel(StatefulUdpServerChannel parent, I udpId, ChannelId id) {
        super(parent, id);
        this.udpId = udpId;
    }

    public I udpId() {
        return udpId;
    }

    public final boolean shouldRegister(Channel ioChannel) {
        if (shouldRegister.getAndSet(false)) {
            this.ioChannel = ioChannel;
            return true;
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public StatefulUdpServerChannel<I, StatefulUdpServerChildChannel<I>> parent() {
        return (StatefulUdpServerChannel<I, StatefulUdpServerChildChannel<I>>) super.parent();
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public ChannelMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean isOpen() {
        return active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    protected SocketAddress localAddress0() {
        return parent().localAddress0();
    }

    @Override
    protected abstract InetSocketAddress remoteAddress0();

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return true;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress remoteAddr, SocketAddress localAddr, ChannelPromise promise) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    protected void doBind(SocketAddress socketAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() {
        if (active) {
            active = false;
            parent().doDisconnect(this);
        }
    }

    @Override
    protected void doClose() {
        doDisconnect();
    }

    @Override
    protected void doBeginRead() {
        // NTD
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {
        Object packet;
        while ((packet = in.current()) != null) {
            try {
                parent().onWrite(this, packet, data -> ioChannel.write(new DatagramPacket(data, remoteAddress0())));
            } finally {
                in.remove();
            }
        }
        ioChannel.flush();
    }
}
