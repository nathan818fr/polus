package com.nathan818.polus.protocol.packet.initial;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.ServerAddrType;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client to connect him to a new server.
 * <p>
 * The client will then repeat the action he was trying to do (by sending needed packets).
 * It's advised to ignore new packets and close the connection soon after sending it.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class RedirectPacket extends PolusPacket {
    private final ServerAddrType serverAddr = new ServerAddrType();

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        serverAddr.read(in);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        serverAddr.write(out);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
