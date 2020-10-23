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
public enum GamePropertyType {
    IS_PUBLIC(1),
    ;

    private static final Int2ObjectMap<GamePropertyType> BY_ID;

    static {
        BY_ID = new Int2ObjectOpenHashMap<>(values().length, 0.25F);
        for (GamePropertyType property : values()) {
            BY_ID.put(property.id(), property);
        }
    }

    public static GamePropertyType byId(int id) {
        return BY_ID.get(id);
    }

    public static GamePropertyType read(ByteBuf in) {
        int id = in.readUnsignedByte();
        GamePropertyType property = byId(id);
        if (property == null) {
            throw new UnknownInputException("GameProperty", id);
        }
        return property;
    }

    public static void write(ByteBuf out, GamePropertyType property) {
        out.writeByte(property.id());
    }

    private final int id;
}
