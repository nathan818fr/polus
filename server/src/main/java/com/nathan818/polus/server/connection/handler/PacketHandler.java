package com.nathan818.polus.server.connection.handler;

import com.google.common.base.Preconditions;
import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.api.util.concurrent.Executor;
import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.DisconnectPacket;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.QuitPacket;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.connection.HandlerConnection;
import com.nathan818.polus.server.connection.PolusConnection;
import com.nathan818.polus.server.connection.ProtocolMapper;
import java.net.InetSocketAddress;
import java.util.Objects;
import lombok.Getter;
import lombok.SneakyThrows;

public abstract class PacketHandler extends AbstractPacketHandler implements PolusConnection {
    private HandlerConnection connection;
    private volatile @Getter boolean active;

    public final void setConnection(HandlerConnection connection) {
        if (this.connection != null) {
            throw new IllegalStateException("connection is already set");
        }
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public final PolusServer getServer() {
        return connection.getServer();
    }

    @Override
    public final Executor getExecutor() {
        return connection.getExecutor();
    }

    @Override
    public long getId() {
        return connection.getId();
    }

    @Override
    public InetSocketAddress getAddress() {
        return connection.getAddress();
    }

    @Override
    public int getProtocolVersion() {
        return connection.getProtocolVersion();
    }

    @Override
    public void setProtocolVersion(int protocolVersion) {
        if (active) {
            connection.setProtocolVersion(protocolVersion);
        }
    }

    @Override
    public String getName() {
        return connection.getName();
    }

    @Override
    public void setName(String name) {
        if (active) {
            connection.setName(name);
        }
    }

    protected final void replace(PacketHandler handler) {
        assertInExecutor();
        if (active) {
            connection.setHandler(handler);
        }
    }

    public void enabled(boolean onConnect) {
        assertInExecutor();
        active = true;
        // NTD
    }

    public void disabled(boolean onDisconnect) {
        assertInExecutor();
        active = false;
        // NTD
    }

    @SneakyThrows
    public void handlePacket(PolusPacket packet) {
        packet.handle(this);
    }

    public boolean handleException(Throwable cause) {
        return false;
    }

    @Override
    public void handle(QuitPacket packet) throws Exception {
        assertInExecutor();
        connection.setDisconnectReason("User has quit");
        connection.disconnect();
    }

    @Override
    public void sendPacket(PolusPacket packet) {
        inExecutorIfActive(() -> connection.send(packet));
    }

    @Override
    public void kick(String message) {
        inExecutorIfActive(() -> connection.disconnect(new DisconnectPacket().customMessage(message)));
    }

    @Override
    public void kick(DisconnectReason reason) {
        inExecutorIfActive(() -> connection.disconnect(new DisconnectPacket().reason(ProtocolMapper.asProtocol(reason))));
    }

    @Override
    public void kick(Throwable cause) {
        inExecutorIfActive(() -> connection.disconnect(cause));
    }

    @Override
    public String toString() {
        return connection == null ? super.toString() : connection.toString();
    }

    protected final void inExecutorIfActive(Runnable task) {
        if (getExecutor().inExecutor()) {
            if (active) {
                task.run();
            }
        } else if (active) {
            getExecutor().execute(() -> {
                if (active) {
                    task.run();
                }
            });
        }
    }

    protected final void assertInExecutor() {
        Preconditions.checkState(getExecutor().inExecutor(), "Called outside the connection executor");
    }
}
