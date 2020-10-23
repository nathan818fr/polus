package com.nathan818.polus.protocol.packet.type;

import com.nathan818.polus.protocol.exception.UnknownInputException;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum GameMapType {
    THE_SKELD(0, 1),
    MIRA_HQ(1, 1 << 1),
    POLUS(2, 1 << 2),
    ;

    private static final Int2ObjectMap<GameMapType> BY_ID;
    private static final GameMapType[] VALUES = values();

    static {
        BY_ID = new Int2ObjectOpenHashMap<>(values().length, 0.25F);
        for (GameMapType map : values()) {
            BY_ID.put(map.id(), map);
        }
    }

    public static GameMapType byId(int id) {
        return BY_ID.get(id);
    }

    public static GameMapType read(ByteBuf in) {
        int id = in.readUnsignedByte();
        GameMapType map = byId(id);
        if (map == null) {
            throw new UnknownInputException("GameMap", id);
        }
        return map;
    }

    public static void write(ByteBuf out, GameMapType map) {
        out.writeByte(map.id());
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static void readSet(ByteBuf in, Set<GameMapType> dst) {
        int bitSet = in.readUnsignedByte();
        dst.clear();
        GameMapType map;
        for (int i = 0; i < VALUES.length; ++i) {
            map = VALUES[i];
            if ((bitSet & map.bitMask()) != 0) {
                dst.add(map);
            }
        }
    }

    public static void writeSet(ByteBuf out, Set<GameMapType> maps) {
        int bitSet = 0;
        for (GameMapType map : maps) {
            bitSet |= map.bitMask();
        }
        out.writeByte(bitSet);
    }

    private final int id;
    private final int bitMask;
}
