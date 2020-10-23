package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.GamePropertyType;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client/server to update a game property.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class GamePropertyPacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private @NonNull GamePropertyType property = GamePropertyType.IS_PUBLIC;
    private boolean value;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        property = GamePropertyType.read(in);
        value = in.readBoolean();
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        GamePropertyType.write(out, property);
        out.writeBoolean(value);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
