package com.nathan818.polus.protocol.packet;

import com.nathan818.polus.protocol.packet.type.DisconnectReasonType;
import io.netty.buffer.ByteBuf;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client to disconnect him.
 * <p>
 * It's advised to close the connection immediately after sending it.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DisconnectPacket extends PolusPacket {
    private @NonNull DisconnectReasonType reason = DisconnectReasonType.CUSTOM;
    private @NonNull String customMessage = "";

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        reason = DisconnectReasonType.read(in);
        if (reason == DisconnectReasonType.CUSTOM) {
            customMessage = readString(in);
        }
    }

    private static final AtomicInteger i = new AtomicInteger(1);

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        DisconnectReasonType.write(out, reason);
        if (reason == DisconnectReasonType.CUSTOM) {
            writeString(out, customMessage);
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
