package com.nathan818.polus.server.connection;

import com.nathan818.polus.api.util.concurrent.Executor;
import com.nathan818.polus.protocol.packet.DisconnectPacket;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.DisconnectReasonType;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.connection.handler.PacketHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public abstract class HandlerConnection {
    private static final AtomicLong ID_COUNTER = new AtomicLong();

    private final PolusServer server;
    private final long id = ID_COUNTER.incrementAndGet();
    private volatile boolean disconnected;
    private String disconnectReason;
    private @Setter @NonNull InetSocketAddress address;
    private @Setter int protocolVersion;
    private @Setter String name;

    public HandlerConnection(PolusServer server, InetSocketAddress address) {
        this.server = server;
        this.address = address;
    }

    public abstract void setHandler(PacketHandler handler);

    public abstract Executor getExecutor();

    public final void send(PolusPacket packet) {
        if (!disconnected) {
            send0(packet);
        }
    }

    protected abstract void send0(PolusPacket packet);

    public void disconnect() {
        disconnect((PolusPacket) null);
    }

    public void disconnect(Throwable cause) {
        disconnect0(cause);
    }

    public void disconnect(PolusPacket packet) {
        if (!disconnected) {
            disconnected = true;
            if (packet instanceof DisconnectPacket) {
                setDisconnectReason((DisconnectPacket) packet);
            }
            disconnect0(packet);
        }
    }

    protected abstract void disconnect0(Throwable cause);

    protected abstract void disconnect0(PolusPacket packet);

    public void markDisconnected() {
        disconnected = true;
    }

    public void setDisconnectReason(String reason) {
        this.disconnectReason = reason;
    }

    public void setDisconnectReason(DisconnectPacket packet) {
        if (packet.reason() == DisconnectReasonType.CUSTOM) {
            setDisconnectReason(packet.customMessage());
        } else {
            setDisconnectReason(packet.reason().name());
        }
    }

    @Override
    public String toString() {
        String name = this.name;
        if (name != null) {
            return "[#" + id + ',' + name + ']';
        }
        return "[#" + id + ']';
    }
}
