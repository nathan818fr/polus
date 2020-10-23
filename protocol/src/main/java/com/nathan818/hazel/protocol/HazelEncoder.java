package com.nathan818.hazel.protocol;

import com.nathan818.hazel.protocol.exception.HazelProtocolException;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HazelEncoder {
    private static final byte HANDSHAKE_VERSION = 0;

    public static ByteBuf encode(HazelPacket packet, IntFunction<ByteBuf> allocator) {
        int size = 1; // id
        if (packet.type().isReliable() != HazelPacketType.ReliableType.NONE) {
            size += 2; // reliableId (short)
        }
        if (packet.type() == HazelPacketType.HELLO) {
            ++size; // handshakeVersion
        }
        if (packet.hasData()) {
            size += packet.data().readableBytes(); // data
        }

        ByteBuf res = allocator.apply(size);
        res.writeByte(packet.type().id()); // id
        if (packet.type().isReliable() != HazelPacketType.ReliableType.NONE) {
            res.writeShort(packet.reliableId()); // reliableId
        }
        if (packet.type() == HazelPacketType.HELLO) {
            res.writeByte(HANDSHAKE_VERSION); // handshakeVersion
        }
        if (packet.hasData()) {
            res.writeBytes(packet.data()); // data
        }
        return res;
    }

    public static HazelPacket decode(ByteBuf in) {
        // id
        byte typeId = in.readByte();
        HazelPacketType type = HazelPacketType.byId(typeId);
        if (type == null) {
            throw new HazelProtocolException("Received unknown HazelPacketType with ID " + typeId);
        }

        int reliableId = -1;
        if (type.isReliable() != HazelPacketType.ReliableType.NONE) {
            // reliableId
            reliableId = in.readUnsignedShort();
        }

        if (type == HazelPacketType.HELLO) {
            // handshakeVersion
            byte handshakeVersion = in.readByte();
            if (handshakeVersion != HANDSHAKE_VERSION) {
                throw new HazelProtocolException("Received unsupported handshake version: " + handshakeVersion);
            }
            if (reliableId != 1) {
                throw new HazelProtocolException("Received HELLO packet with an invalid reliableId: " + reliableId);
            }
        }

        ByteBuf data = null;
        int dataLen = in.readableBytes();
        if (dataLen > 0) {
            // data
            data = in.readSlice(dataLen);
        }

        return new HazelPacket(type, reliableId, data);
    }
}
