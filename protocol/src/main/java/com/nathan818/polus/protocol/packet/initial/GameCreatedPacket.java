package com.nathan818.polus.protocol.packet.initial;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.game.JoinGamePacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client to let him know that his game has been created successfully.
 * <p>
 * The client will then send a {@link JoinGamePacket} to this game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class GameCreatedPacket extends PolusPacket {
    private int gameCode;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
