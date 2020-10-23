package com.nathan818.polus.protocol.packet.game;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.DisconnectReasonType;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Packet sent to the client when a player quit a game.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class RemovePlayerPacket extends PolusPacket implements GamePacket {
    private int gameCode;
    private int playerId;
    private int hostId;
    private @NonNull DisconnectReasonType reason = DisconnectReasonType.PLAYER_QUIT;

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        gameCode = in.readIntLE();
        playerId = in.readIntLE();
        hostId = in.readIntLE();
        reason = DisconnectReasonType.read(in);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeIntLE(gameCode);
        out.writeIntLE(playerId);
        out.writeIntLE(hostId);
        DisconnectReasonType.write(out, reason);
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
