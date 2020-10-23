package com.nathan818.polus.protocol;

import com.nathan818.hazel.protocol.HazelPacket;
import com.nathan818.hazel.protocol.HazelPacketType;
import com.nathan818.polus.protocol.packet.DisconnectPacket;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.PolusPacket.Recipient;
import com.nathan818.polus.protocol.packet.initial.LoginPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class PolusEncoder extends MessageToMessageEncoder<PolusPacket> {
    private final Recipient recipient;
    private boolean preferDirect = true;

    @Override
    protected void encode(ChannelHandlerContext ctx, PolusPacket in, List<Object> out) {
        ByteBuf buf = null;
        try {
            HazelPacketType packetType;
            buf = allocateBuffer(ctx);
            if (in instanceof LoginPacket) {
                packetType = HazelPacketType.HELLO;
                in.write(buf, recipient);
            } else if (in instanceof DisconnectPacket) {
                packetType = HazelPacketType.DISCONNECT;
                buf.writeByte(1);
                writeMessage(buf, 0, in);
            } else {
                packetType = in.reliable() ? HazelPacketType.RELIABLE : HazelPacketType.UNRELIABLE;
                int packetId = PolusProtocol.get(!recipient.isServerChannel()).getPacketId(in.getClass());
                writeMessage(buf, packetId, in);
            }

            out.add(new HazelPacket(packetType, buf));
            buf = null;
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }

    private ByteBuf allocateBuffer(ChannelHandlerContext ctx) {
        return preferDirect ? ctx.alloc().ioBuffer() : ctx.alloc().heapBuffer();
    }

    private void writeMessage(ByteBuf buf, int id, PolusPacket in) {
        int dataLenIndex;
        buf.writerIndex((dataLenIndex = buf.writerIndex()) + 2);
        buf.writeByte(id);
        in.write(buf, recipient);
        buf.setShortLE(dataLenIndex, buf.writerIndex() - dataLenIndex - 3);
    }
}
