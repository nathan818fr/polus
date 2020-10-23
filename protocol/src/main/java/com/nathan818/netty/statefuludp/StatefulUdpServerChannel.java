package com.nathan818.netty.statefuludp;

import com.nathan818.netty.statefuludp.util.DefaultChannelGroupFuture;
import com.nathan818.netty.statefuludp.util.DefaultChannelGroupFutures;
import com.nathan818.netty.statefuludp.util.ServerBootstrapAcceptor;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AbstractServerChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.MultithreadEventExecutorGroup;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StatefulUdpServerChannel<I, C extends StatefulUdpServerChildChannel<I>>
        extends AbstractServerChannel {
    private static final Logger logger = LoggerFactory.getLogger(StatefulUdpServerChannel.class);

    private final ChannelFactory<? extends DatagramChannel> channelFactory;
    private final int threadsCount;
    private final StatefulUdpChannelConfig config = new StatefulUdpChannelConfig(this);

    private volatile int state;
    private volatile SocketAddress localAddress;

    private List<Bootstrap> listenersBootstraps;
    private List<Channel> listenersChannels;
    private final Map<I, C> childrenChannels = new ConcurrentHashMap<>(16, 0.5F);

    public StatefulUdpServerChannel(ChannelFactory<? extends DatagramChannel> channelFactory, int threadsCount) {
        this.channelFactory = Objects.requireNonNull(channelFactory, "channelFactory cannot be null!");
        this.threadsCount = threadsCount;
    }

    @Override
    public StatefulUdpChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return state < 2;
    }

    @Override
    public boolean isActive() {
        return state == 1;
    }

    @Override
    protected SocketAddress localAddress0() {
        return localAddress;
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return true;
    }

    @Override
    protected void doRegister() {
        int threadsCount = this.threadsCount;
        if (threadsCount == 0) {
            EventLoopGroup eventLoopGroup = eventLoop().parent();
            if (eventLoopGroup instanceof MultithreadEventExecutorGroup) {
                threadsCount = ((MultithreadEventExecutorGroup) eventLoopGroup).executorCount();
            } else {
                threadsCount = 1;
            }
        }
        if (threadsCount <= 0) {
            throw new IllegalArgumentException("threadsCount must be strictly positive");
        }

        listenersBootstraps = new ArrayList<>(threadsCount);
        listenersChannels = new ArrayList<>(threadsCount);
        for (int i = 0; i < threadsCount; ++i) {
            listenersBootstraps.add(createDatagramBootstrap(channelFactory, threadsCount));
        }

        pipeline().addFirst(new BossHandler());
    }

    private Bootstrap createDatagramBootstrap(
            ChannelFactory<? extends DatagramChannel> channelFactory, int threadsCount) {
        Bootstrap b = new Bootstrap()
                .group(eventLoop().parent())
                .channelFactory(channelFactory)
                .handler(new ListenerHandler());
        copyOptions(this.config().getOptions(), b);
        if (threadsCount > 1) {
            b.option(ChannelOption.valueOf("io.netty.channel.unix.UnixChannelOption#SO_REUSEPORT"), true);
        }
        return b;
    }

    @Override
    protected final void doBind(SocketAddress socketAddr) {
        throw new IllegalStateException("bind must be handled by " + BossHandler.class);
    }

    protected void doBind(ChannelHandlerContext ctx, SocketAddress socketAddr, ChannelPromise promise) {
        List<ChannelFuture> binds = new ArrayList<>();
        for (Bootstrap b : listenersBootstraps) {
            ChannelFuture future;
            try {
                future = b.bind(socketAddr);
            } catch (Throwable t) {
                future = ctx.newFailedFuture(t);
            }
            binds.add(future);
        }
        ChannelGroupFuture bindFutures = new DefaultChannelGroupFuture(null, binds, eventLoop());
        bindFutures.addListener(f -> {
            if (f.isSuccess()) {
                for (ChannelFuture bindFuture : bindFutures) {
                    if (localAddress == null) {
                        localAddress = bindFuture.channel().localAddress();
                    }
                    listenersChannels.add(bindFuture.channel());
                }

                state = 1;
                promise.setSuccess();
            } else {
                for (ChannelFuture bindFuture : bindFutures) {
                    if (bindFuture.isSuccess()) {
                        bindFuture.channel().close();
                    }
                }

                promise.setFailure(DefaultChannelGroupFutures.flattenException(f.cause()));
            }
        });
    }

    @Override
    protected final void doClose() {
        throw new IllegalStateException("close must be handled by " + BossHandler.class);
    }

    protected void doClose(ChannelHandlerContext ctx, ChannelPromise promise) {
        if (state <= 1) {
            localAddress = null;
            state = 2; // mark inactive (prevent new read or accept)

            // close listeners, then children (and always succeed the promise)
            closeListeners().addListener(ignored -> {
                closeChildren().addListener(ignored2 -> {
                    promise.setSuccess();
                });
            });
        } else {
            // already closed (just succeed the promise)
            promise.setSuccess();
        }
    }

    private ChannelGroupFuture closeListeners() {
        // close listeners (and immediately clear listenersChannels)
        List<ChannelFuture> closes = listenersChannels.stream().map(Channel::close).collect(Collectors.toList());
        listenersChannels.clear();
        return new DefaultChannelGroupFuture(null, closes, eventLoop());
    }

    private ChannelGroupFuture closeChildren() {
        // close children (childrenChannels will be cleared by StatefulUdpServerChildChannel#doDisconnect)
        List<ChannelFuture> closes = childrenChannels.values().stream().map(Channel::close)
                .collect(Collectors.toList());
        return new DefaultChannelGroupFuture(null, closes, eventLoop());
    }

    @Override
    protected void doBeginRead() {
        // NTD
    }

    protected void doDisconnect(C childChannel) {
        if (childrenChannels.remove(childChannel.udpId(), childChannel)) {
            onClose(childChannel);
        }
    }

    protected final C registerChildChannel(Channel holderChannel, I udpId) {
        C childChannel = childrenChannels.computeIfAbsent(udpId, this::createChildChannel);
        if (childChannel.shouldRegister(holderChannel)) {
            holderChannel.pipeline().fireChannelRead(childChannel);
            holderChannel.pipeline().fireChannelReadComplete();
            onRegister(childChannel);
        }
        return childChannel;
    }

    protected final C getChildChannel(I udpId) {
        return childrenChannels.get(udpId);
    }

    protected abstract C createChildChannel(I udpId);

    protected abstract void onRegister(C childChannel);

    protected abstract void onRead(ChannelHandlerContext ctx, DatagramPacket packet, BiConsumer<C, Object> list);

    protected abstract void onWrite(C childChannel, Object packet, Consumer<ByteBuf> list);

    protected abstract void onClose(C childChannel);

    protected class BossHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress socketAddr, ChannelPromise promise) {
            doBind(ctx, socketAddr, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            doClose(ctx, promise);
        }
    }

    protected class ListenerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) {
            ServerBootstrapAcceptor.copy(StatefulUdpServerChannel.this, ctx.channel());
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
            if (!StatefulUdpServerChannel.this.isActive()) {
                return;
            }
            onRead(ctx, packet, (c, p) -> c.pipeline().fireChannelRead(p));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void copyOptions(Map<ChannelOption<?>, Object> options, Bootstrap bootstrap) {
        if (options == null) {
            return;
        }
        for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet()) {
            bootstrap.option((ChannelOption) e.getKey(), e.getValue());
        }
    }
}
