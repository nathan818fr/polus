package com.nathan818.hazel.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
public final class HazelPacket implements ByteBufHolder {
    private static final ByteBuf EMPTY_BUFFER = Unpooled.wrappedBuffer(new byte[0]);

    private HazelPacketType type;
    private int reliableId = -1;
    private ByteBuf data;

    public HazelPacket(HazelPacketType type) {
        this.type = type;
    }

    public HazelPacket(HazelPacketType type, int reliableId) {
        this.type = type;
        reliableId(reliableId);
    }

    public HazelPacket(HazelPacketType type, ByteBuf data) {
        this.type = type;
        this.data = data;
    }

    public HazelPacket(HazelPacketType type, int reliableId, ByteBuf data) {
        this.type = type;
        reliableId(reliableId);
        this.data = data;
    }

    public HazelPacket reliableId(int reliableId) {
        if ((reliableId < 0 || reliableId > 65535) && reliableId != -1) {
            throw new IllegalArgumentException("reliableId must be an unsigned short (or -1)");
        }
        this.reliableId = reliableId;
        return this;
    }

    public boolean hasReliableId() {
        return reliableId != -1;
    }

    public boolean hasData() {
        return data != null;
    }

    @Override
    public ByteBuf content() {
        return data == null ? EMPTY_BUFFER : data;
    }

    @Override
    public HazelPacket copy() {
        return replace(data == null ? null : data.copy());
    }

    @Override
    public HazelPacket duplicate() {
        return replace(data == null ? null : data.duplicate());
    }

    @Override
    public HazelPacket retainedDuplicate() {
        return replace(data == null ? null : data.retainedDuplicate());
    }

    @Override
    public HazelPacket replace(ByteBuf data) {
        return new HazelPacket(type, reliableId, data);
    }

    @Override
    public int refCnt() {
        return data == null ? 1 : data.refCnt();
    }

    @Override
    public HazelPacket retain() {
        if (data != null) {
            data.retain();
        }
        return this;
    }

    @Override
    public HazelPacket retain(int increment) {
        if (data != null) {
            data.retain(increment);
        }
        return this;
    }

    @Override
    public HazelPacket touch() {
        if (data != null) {
            data.touch();
        }
        return this;
    }

    @Override
    public HazelPacket touch(Object hint) {
        if (data != null) {
            data.touch(hint);
        }
        return this;
    }

    @Override
    public boolean release() {
        if (data != null) {
            return data.release();
        }
        return false;
    }

    @Override
    public boolean release(int decrement) {
        if (data != null) {
            return data.release(decrement);
        }
        return false;
    }
}
