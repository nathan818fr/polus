package com.nathan818.polus.protocol.packet;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the server to indicate that the client is disconnecting.
 * <p>
 * It's advised to close the connection immediately after receiving it.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class QuitPacket extends PolusPacket {
    @Override
    public void read(ByteBuf in, Recipient recipient) {
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
