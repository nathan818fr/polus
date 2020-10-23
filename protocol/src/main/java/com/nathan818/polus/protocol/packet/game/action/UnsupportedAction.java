package com.nathan818.polus.protocol.packet.game.action;

import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsupportedAction extends GameAction {
    private byte[] data;

    @Override
    public void read(ByteBuf in, PolusPacket.Recipient recipient) {
        data = new byte[in.readableBytes()];
        in.readBytes(data);
    }

    @Override
    public void write(ByteBuf out, PolusPacket.Recipient recipient) {
        out.writeBytes(data);
    }

    @Override
    public void handle(int targetId, AbstractActionHandler handler) throws Exception {
        handler.handle(targetId, this);
    }
}
