package com.nathan818.polus.protocol.packet.initial;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Packet sent to the server to authenticate.
 * <p>
 * This packet is sent only once (and first).
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginPacket extends PolusPacket {
    private int protocolVersion;
    private @NonNull String playerName = "";

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        protocolVersion = in.readIntLE();
        playerName = readString(in);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(protocolVersion);
        writeString(out, playerName);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
