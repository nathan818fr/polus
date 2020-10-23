package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client when a new player joins a game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class AddPlayerPacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private int playerId;
    private int hostId;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        playerId = in.readIntLE();
        hostId = in.readIntLE();
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        out.writeIntLE(playerId);
        out.writeIntLE(hostId);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
