package com.nathan818.hazel.protocol;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@ToString
public enum HazelPacketType {
    UNRELIABLE(0x00, ReliableType.NONE),
    RELIABLE(0x01, ReliableType.IN),
    HELLO(0x08, ReliableType.IN),
    DISCONNECT(0x09, ReliableType.NONE),
    ACKNOWLEDGEMENT(0x0A, ReliableType.OUT),
    PING(0x0C, ReliableType.IN),
    ;

    private static final Byte2ObjectMap<HazelPacketType> BY_ID;

    static {
        BY_ID = new Byte2ObjectOpenHashMap<>(values().length, 0.25F);
        for (HazelPacketType packetType : values()) {
            BY_ID.put(packetType.id(), packetType);
        }
    }

    public static HazelPacketType byId(byte packetId) {
        return BY_ID.get(packetId);
    }

    private final byte id;
    private final ReliableType isReliable;

    HazelPacketType(int id, ReliableType isReliable) {
        this.id = (byte) id;
        this.isReliable = isReliable;
    }

    public enum ReliableType {
        NONE,
        IN,
        OUT,
    }
}
