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
 * Packet sent to the server to retrieve the games list.
 * <p>
 * This packet is first sent directly after the {@link LoginPacket}.
 * Then it can be sent again as long as no game has been joined.
 */
@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class FindGamePacket extends PolusPacket {
    private int _field1; // UNKNOWN: Seem to be always "0"; Supposed to be a byte (but can potentially be a VarInt)
    private final GameOptionsType options = new GameOptionsType();

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        _field1 = in.readUnsignedByte();
        options.read(readVarBytes(in), true);
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        out.writeByte(_field1);
        Writer w = beginVarBytes(out);
        options.write(w.buf(), true);
        w.end();
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
