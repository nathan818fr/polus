package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.game.action.GameAction;
import com.nathan818.polus.protocol.packet.game.action.UnsupportedAction;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client/server to perform an action in a game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class GameActionPacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private GameAction action;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        // TODO: decode actions
        action = new UnsupportedAction();
        action.read(in, recipient);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        // TODO: encode actions
        action.write(out, recipient);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
