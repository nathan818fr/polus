package com.nathan818.polus.api.connection;

import com.nathan818.polus.api.Server;
import com.nathan818.polus.api.util.concurrent.Executor;
import java.net.InetSocketAddress;

public interface Connection {
    Server getServer();

    long getId();

    Executor getExecutor();

    InetSocketAddress getAddress();

    int getProtocolVersion();

    String getName();

    boolean isActive();

    void kick(String message);

    void kick(DisconnectReason reason);

    void kick(Throwable cause);
}
