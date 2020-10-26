package com.nathan818.polus.server.netty;

import com.nathan818.polus.api.util.concurrent.AbstractExecutor;
import io.netty.channel.EventLoop;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@Slf4j
public class EventLoopExecutor extends AbstractExecutor {
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
                log.error("Unexpected exception from a GameExecutor:", t);
            }
        };
    }
}
