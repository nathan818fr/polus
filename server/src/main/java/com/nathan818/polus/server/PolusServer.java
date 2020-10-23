package com.nathan818.polus.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nathan818.hazel.protocol.HazelServerChannel;
import com.nathan818.polus.api.Server;
import com.nathan818.polus.logging.PolusLogging;
import com.nathan818.polus.server.game.PolusGameManager;
import com.nathan818.polus.server.netty.PipelineUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolusServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger(PolusServer.class);

    private volatile @Getter boolean isRunning;
    private final ReentrantLock shutdownLock = new ReentrantLock();

    private @Getter EventLoopGroup eventLoops;
    private final Collection<Channel> listeners = new HashSet<>();

    private @Getter PolusGameManager gameManager;

    public void start() throws Exception {
        eventLoops = PipelineUtil.newEventLoopGroup(0, new ThreadFactoryBuilder().setNameFormat("Netty IO Thread #%1$d").build());

        // TODO: load config

        gameManager = new PolusGameManager(this, new DefaultEventLoopGroup(1, new ThreadFactoryBuilder().setNameFormat("Game Thread #%1$d").build()));

        // PLANNED: load plugins

        isRunning = true;

        // PLANNED: enable plugins

        startListeners();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop0(false), "ShutdownHook Thread"));
    }

    public void startListeners() {
        InetSocketAddress socketAddr = new InetSocketAddress(22023); // TODO: config

        ChannelFactory<DatagramChannel> channelFactory = new ReflectiveChannelFactory<>(PipelineUtil.getDatagramChannel());

        ChannelHandler childHandler = new PipelineUtil.ChildInitializer(this);

        new ServerBootstrap()
                .group(eventLoops)
                .channelFactory(() -> new HazelServerChannel(channelFactory, PipelineUtil.isEpoll() ? 0 : 1))
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(childHandler)
                .localAddress(socketAddr)
                .bind()
                .addListener((GenericFutureListener<ChannelFuture>) f -> {
                    if (f.isSuccess()) {
                        listeners.add(f.channel());
                        logger.info("Listening on " + socketAddr);
                    } else {
                        logger.warn("Could not bind to host " + socketAddr, f.cause());
                    }
                });
    }

    public void stopListeners() {
        for (Channel listener : listeners) {
            logger.info("Closing listener " + listener);
            try {
                listener.close().syncUninterruptibly();
            } catch (ChannelException ex) {
                logger.error("Could not close listen thread", ex);
            }
        }
        listeners.clear();
    }

    @Override
    public void stop() {
        new Thread(() -> stop0(true), "Shutdown Thread").start();
    }

    private void stop0(boolean callSystemExit) {
        shutdownLock.lock();
        try {
            if (!isRunning) {
                return;
            }
            isRunning = false;

            logger.info("Stopping...");

            stopListeners();

            logger.info("Closing IO threads");
            eventLoops.shutdownGracefully();
            try {
                eventLoops.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ignored) {
            }

            logger.info("Stopped!");
        } finally {
            shutdownLock.unlock();
        }

        PolusLogging.shutdown();

        if (callSystemExit) {
            System.exit(0);
        }
    }
}
