package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client/server to end a game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class EndGamePacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private int _field2;
    private int _field3;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        _field2 = in.readUnsignedByte();
        _field3 = in.readUnsignedByte();
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        out.writeByte(_field2);
        out.writeByte(_field3);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
