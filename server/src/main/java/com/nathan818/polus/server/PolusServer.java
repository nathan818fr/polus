package com.nathan818.polus.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.nathan818.hazel.protocol.HazelServerChannel;
import com.nathan818.polus.api.Server;
import com.nathan818.polus.server.config.Config;
import com.nathan818.polus.server.config.ListenerConfig;
import com.nathan818.polus.server.config.YamlConfigProvider;
import com.nathan818.polus.server.game.PolusGameManager;
import com.nathan818.polus.server.netty.PipelineUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

@Slf4j
public class PolusServer implements Server {
    private Config config;
    private Runnable stopCallback;
    private volatile @Getter boolean isRunning;
    private final ReentrantLock shutdownLock = new ReentrantLock();

    private EventLoopGroup gameEventLoops;
    private @Getter PolusGameManager gameManager;

    private EventLoopGroup networkEventLoops;
    private final Collection<Channel> listeners = new HashSet<>();

    public boolean start(File configFile, Runnable stopCallback) throws Exception {
        this.stopCallback = stopCallback;
        if ((config = loadConfig(configFile)) == null) {
            return false;
        }

        int gameThreads = config.getGameThreads(Runtime.getRuntime().availableProcessors());
        gameEventLoops = new DefaultEventLoopGroup(gameThreads, new ThreadFactoryBuilder().setNameFormat("Game Thread #%1$d").build());

        int networkThreads = config.getNetworkThreads(PipelineUtil.isEpoll() ? Runtime.getRuntime().availableProcessors() : 1);
        if (!PipelineUtil.isEpoll() && networkThreads > 1) {
            networkThreads = 1;
            log.warn("Epoll is not available so networkThreads is capped to 1");
        }
        networkEventLoops = PipelineUtil.newEventLoopGroup(networkThreads, new ThreadFactoryBuilder().setNameFormat("Netty IO Thread #%1$d").build());

        gameManager = new PolusGameManager(this, gameEventLoops);

        // PLANNED: load plugins

        isRunning = true;

        // PLANNED: enable plugins

        if (!startListeners()) {
            return false;
        }

        return true;
    }

    public Config loadConfig(File configFile) {
        log.debug("Using configuration file: '" + configFile + "'");

        // read file
        Config config;
        try {
            InputStream is = new FileInputStream(configFile);
            config = YamlConfigProvider.readConfig(is);
        } catch (FileNotFoundException ignored) {
            log.info("The configuration file does not exist, it will be created");
            config = null;
        } catch (Exception ex) {
            throw new RuntimeException("Error reading the configuration file", ex);
        }
        if (config == null) {
            config = new Config();
        }

        // transform (init defaults)
        if (config.getListeners() == null || config.getListeners().isEmpty()) {
            config.setListeners(new ArrayList<>(Collections.singletonList(new ListenerConfig())));
        }

        // save file (to apply changes and ensure that same formatting is used by everybody)
        try {
            //noinspection ResultOfMethodCallIgnored
            configFile.getParentFile().mkdirs();
            YamlConfigProvider.writeConfig(new FileOutputStream(configFile), config);
        } catch (Exception ex) {
            log.warn("Error saving the configuration file", ex);
        }

        // validate
        Validator validator = Validation.byDefaultProvider().configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Config>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            log.error("The configuration file contains invalid values:\n"
                    + violations.stream()
                    .map(v -> "- " + v.getPropertyPath() + " " + v.getMessage() + " (current value: " + v.getInvalidValue() + ")")
                    .distinct()
                    .collect(Collectors.joining("\n")));
            return null;
        }

        return config;
    }

    public boolean startListeners() {
        ChannelFactory<DatagramChannel> channelFactory = new ReflectiveChannelFactory<>(PipelineUtil.getDatagramChannel());
        ChannelHandler childHandler = new PipelineUtil.ChildInitializer(this);

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(networkEventLoops)
                .channelFactory(() -> new HazelServerChannel(channelFactory, PipelineUtil.isEpoll() ? 0 : 1))
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(childHandler);

        List<ChannelPromise> binds = new ArrayList<>();
        for (ListenerConfig listener : config.getListeners()) {
            InetSocketAddress socketAddr = new InetSocketAddress(listener.getIp(), listener.getPort());
            ChannelFuture bindFuture = bootstrap.bind(socketAddr);
            ChannelPromise promise = bindFuture.channel().newPromise();
            bindFuture.addListener((GenericFutureListener<ChannelFuture>) f -> {
                if (f.isSuccess()) {
                    listeners.add(f.channel());
                    log.info("Listening on " + socketAddr);
                    promise.setSuccess();
                } else {
                    log.warn("Could not bind to host " + socketAddr, f.cause());
                    promise.setFailure(f.cause());
                }
            });
            binds.add(promise);
        }

        for (ChannelPromise bind : binds) {
            bind.awaitUninterruptibly();
        }
        if (listeners.isEmpty()) {
            log.error("No listeners could be started");
            return false;
        }

        return true;
    }

    public void stopListeners() {
        for (Channel listener : listeners) {
            log.info("Closing listener " + listener);
            try {
                listener.close().syncUninterruptibly();
            } catch (ChannelException ex) {
                log.error("Could not close listen thread", ex);
            }
        }
        listeners.clear();
    }

    @Override
    public void stop() {
        new Thread(this::stopNow, "Shutdown Thread").start();
    }

    void stopNow() {
        shutdownLock.lock();
        try {
            if (!isRunning) {
                return;
            }
            isRunning = false;
        } finally {
            shutdownLock.unlock();
        }

        log.info("Stopping...");

        stopListeners();

        // TODO: stop games

        log.info("Closing event loops");
        networkEventLoops.shutdownGracefully();
        gameEventLoops.shutdownGracefully();
        try {
            networkEventLoops.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            gameEventLoops.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException ignored) {
        }

        log.info("Stopped!");

        stopCallback.run();
    }
}
