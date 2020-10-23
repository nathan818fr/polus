package com.nathan818.polus.protocol.packet;

import com.nathan818.polus.protocol.exception.InputOverflowException;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@Setter
public abstract class PolusPacket {
    private boolean reliable = true;

    public abstract void read(ByteBuf in, Recipient recipient);

    public abstract void write(ByteBuf out, Recipient recipient);

    public abstract void handle(AbstractPacketHandler handler) throws Exception;

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    public interface Recipient {
        boolean isServerChannel();

        int getProtocolVersion();
    }

    public static int readVarInt(ByteBuf in) {
        return readVarInt(in, 5);
    }

    public static int readVarInt(ByteBuf in, int maxBytes) {
        int ret = 0;
        int shift = 0;
        byte b;
        do {
            b = in.readByte();
            ret |= (b & 0x7F) << (shift++ * 7);

            if (shift > maxBytes) {
                throw new InputOverflowException("VarInt", shift, maxBytes);
            }
        } while ((b & 0x80) == 0x80);
        return ret;
    }

    public static void writeVarInt(ByteBuf out, int value) {
        int part;
        do {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            out.writeByte(part);
        } while (value != 0);
    }

    public static Message readMessage(ByteBuf in) {
        return readMessage(in, Short.MAX_VALUE);
    }

    public static Message readMessage(ByteBuf in, int maxBytes) {
        int len = readVarInt(in);
        if (len > maxBytes) {
            throw new InputOverflowException("Message", len, maxBytes);
        }
        int id = in.readUnsignedByte();
        return new Message(id, in.readSlice(len));
    }

    public static Writer beginMessage(ByteBuf in, int id) {
        return new MessageWriter(in, id);
    }

    public static ByteBuf readVarBytes(ByteBuf in) {
        return readVarBytes(in, Short.MAX_VALUE);
    }

    public static ByteBuf readVarBytes(ByteBuf in, int maxBytes) {
        int len = readVarInt(in);
        if (len > maxBytes) {
            throw new InputOverflowException("VarBytes", len, maxBytes);
        }
        return in.readSlice(len);
    }

    public static Writer beginVarBytes(ByteBuf out) {
        return new VarBytesWriter(out);
    }

    public static void readVarIntArray(ByteBuf in, IntList dst) {
        readVarIntArray(in, dst, Short.MAX_VALUE / 4);
    }

    public static void readVarIntArray(ByteBuf in, IntList dst, int maxEntries) {
        int len = readVarInt(in);
        if (len > maxEntries) {
            throw new InputOverflowException("VarIntArray", len, maxEntries, "entries");
        }
        checkReadableBytes(in, len); // check that at least {len} bytes are readable

        dst.clear();
        ensureCapacity(dst, len);
        for (int i = 0; i < len; i++) {
            dst.add(readVarInt(in));
        }
    }

    public static void writeVarIntArray(ByteBuf out, IntList values) {
        int l;
        writeVarInt(out, l = values.size());
        for (int i = 0; i < l; i++) {
            writeVarInt(out, values.getInt(i));
        }
    }

    public static String readString(ByteBuf in) {
        return readString(in, Short.MAX_VALUE);
    }

    public static String readString(ByteBuf in, int maxBytes) {
        int len = readVarInt(in);
        if (len > maxBytes) {
            throw new InputOverflowException("String", len, maxBytes);
        }

        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf out, String value) {
        if (value.isEmpty()) {
            out.writeByte(0);
        } else {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            writeVarInt(out, bytes.length);
            out.writeBytes(bytes);
        }
    }

    private static void checkReadableBytes(ByteBuf buf, int len) {
        if (len > buf.readableBytes()) {
            throw new IndexOutOfBoundsException(String.format(
                    "readerIndex(%d) + length(%d) exceeds writerIndex(%d): %s",
                    buf.readerIndex(), len, buf.writerIndex(), buf));
        }
    }

    private static void ensureCapacity(IntList list, int capacity) {
        if (list instanceof IntArrayList) {
            ((IntArrayList) list).ensureCapacity(capacity);
        }
    }

    public interface Writer {
        ByteBuf buf();

        void end();
    }

    private static class MessageWriter implements Writer {
        private final ByteBuf out;
        private final int lenIndex;

        private MessageWriter(ByteBuf out, int id) {
            this.out = out;
            out.writerIndex((lenIndex = out.writerIndex()) + 2);
            out.writeByte(id);
        }

        public ByteBuf buf() {
            return this.out;
        }

        public void end() {
            out.setShortLE(lenIndex, out.writerIndex() - lenIndex - 3);
        }
    }

    private static class VarBytesWriter implements Writer {
        private final ByteBuf out;
        private final ByteBuf buf;

        public VarBytesWriter(ByteBuf out) {
            this.out = out;
            buf = out.alloc().buffer();
        }

        @Override
        public ByteBuf buf() {
            return buf;
        }

        @Override
        public void end() {
            writeVarInt(out, buf.readableBytes());
            out.writeBytes(buf);
            buf.release();
        }
    }

    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Data
    public static class Message {
        private final int id;
        private final ByteBuf buf;
    }
}
