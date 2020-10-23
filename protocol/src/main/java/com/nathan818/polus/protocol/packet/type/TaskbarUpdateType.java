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
public enum TaskbarUpdateType {
    ALWAYS(0),
    MEETINGS(1),
    NEVER(2),
    ;

    private static final Int2ObjectMap<TaskbarUpdateType> BY_ID;

    static {
        BY_ID = new Int2ObjectOpenHashMap<>(values().length, 0.25F);
        for (TaskbarUpdateType updateMode : values()) {
            BY_ID.put(updateMode.id(), updateMode);
        }
    }

    public static TaskbarUpdateType byId(int id) {
        return BY_ID.get(id);
    }

    public static TaskbarUpdateType read(ByteBuf in) {
        int id = in.readUnsignedByte();
        TaskbarUpdateType updateMode = byId(id);
        if (updateMode == null) {
            throw new UnknownInputException("TaskbarUpdateMode", id);
        }
        return updateMode;
    }

    public static void write(ByteBuf out, TaskbarUpdateType updateMode) {
        out.writeByte(updateMode.id());
    }

    private final int id;
}
