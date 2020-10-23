package com.nathan818.polus.protocol.packet.type;

import com.nathan818.polus.protocol.exception.UnknownInputException;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum KillDistanceType {
    SHORT(0),
    MEDIUM(1),
    LONG(2),
    ;

    private static final Int2ObjectMap<KillDistanceType> BY_ID;

    static {
        BY_ID = new Int2ObjectOpenHashMap<>(values().length, 0.25F);
        for (KillDistanceType type : values()) {
            BY_ID.put(type.id(), type);
        }
    }

    public static KillDistanceType byId(int id) {
        return BY_ID.get(id);
    }

    public static KillDistanceType read(ByteBuf in) {
        int id = in.readUnsignedByte();
        KillDistanceType killDistance = byId(id);
        if (killDistance == null) {
            throw new UnknownInputException("KillDistance", id);
        }
        return killDistance;
    }

    public static void write(ByteBuf out, KillDistanceType killDistance) {
        out.writeByte(killDistance.id());
    }

    private final int id;
}
