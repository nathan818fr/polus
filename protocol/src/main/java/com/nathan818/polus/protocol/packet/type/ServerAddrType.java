package com.nathan818.polus.protocol.packet.type;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
public class ServerAddrType {
    private static final byte[] EMPTY_IP = new byte[4];

    private @NonNull byte[] ip = EMPTY_IP;
    private int port;

    public ServerAddrType ip(byte[] ip) {
        if (ip == null) {
            throw new NullPointerException("ip is marked non-null but is null");
        }
        if (ip.length != 4) {
            throw new IllegalArgumentException("ip must be 4 bytes (IPv4)");
        }
        this.ip = ip;
        return this;
    }

    public void read(ByteBuf in) {
        ip = new byte[4];
        in.readBytes(ip);
        port = in.readShortLE();
    }

    public void write(ByteBuf out) {
        out.writeBytes(ip);
        out.writeShortLE(port);
    }
}
