package com.nathan818.polus.protocol.packet.initial;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.GameOptionsType;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the server to create a game.
 * <p>
 * This packet is sent only once - directly after the {@link LoginPacket}.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class CreateGamePacket extends PolusPacket {
    private final GameOptionsType options = new GameOptionsType();

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        options.read(readVarBytes(in), false);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        Writer w = beginVarBytes(out);
        options.write(w.buf(), false);
        w.end();
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
