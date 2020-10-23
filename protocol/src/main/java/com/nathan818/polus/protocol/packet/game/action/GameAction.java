package com.nathan818.polus.protocol.packet.game.action;

import com.nathan818.polus.protocol.packet.PolusPacket;
import io.netty.buffer.ByteBuf;

public abstract class GameAction {
    public abstract void read(ByteBuf in, PolusPacket.Recipient recipient);

    public abstract void write(ByteBuf out, PolusPacket.Recipient recipient);

    public abstract void handle(int targetId, AbstractActionHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
