package com.nathan818.polus.server.netty;

import com.nathan818.polus.api.util.concurrent.AbstractExecutor;
import io.netty.channel.EventLoop;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@ToString
public class EventLoopExecutor extends AbstractExecutor {
    private static final Logger logger = LoggerFactory.getLogger(EventLoopExecutor.class);

    private final EventLoop eventLoop;

    public EventLoopExecutor(EventLoop eventLoop) {
        super(eventLoop);
        this.eventLoop = eventLoop;
    }

    @Override
    public boolean inExecutor() {
        return eventLoop.inEventLoop();
    }

    @Override
    public boolean inExecutor(Thread thread) {
        return eventLoop.inEventLoop(thread);
    }

    @Override
    protected Runnable wrap(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error("Unexpected exception from a GameExecutor:", t);
            }
        };
    }
}
