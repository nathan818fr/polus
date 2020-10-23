package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the server to join a game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class JoinGamePacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private int _field2 = 7; // UNKNOWN: Seem to be always "7"; Supposed to be a byte (but can potentially be a VarInt)

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        _field2 = in.readUnsignedByte();
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        out.writeByte(_field2);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
