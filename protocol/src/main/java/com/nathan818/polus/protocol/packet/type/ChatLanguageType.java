package com.nathan818.polus.protocol.packet.type;

import io.netty.buffer.ByteBuf;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum ChatLanguageType {
    OTHER(1),
    SPANISH(1 << 1),
    KOREAN(1 << 2),
    RUSSIAN(1 << 3),
    PORTUGUESE(1 << 4),
    ARABIC(1 << 5),
    FILIPINO(1 << 6),
    POLISH(1 << 7),
    ENGLISH(1 << 8),
    ;

    private static final ChatLanguageType[] VALUES = values();

    @SuppressWarnings("ForLoopReplaceableByForEach")
    public static void readSet(ByteBuf in, Set<ChatLanguageType> dst) {
        int bitSet = in.readIntLE();
        dst.clear();
        ChatLanguageType language;
        for (int i = 0; i < VALUES.length; ++i) {
            language = VALUES[i];
            if ((bitSet & language.bitMask()) != 0) {
                dst.add(language);
            }
        }
    }

    public static void writeSet(ByteBuf out, Set<ChatLanguageType> languages) {
        int bitSet = 0;
        for (ChatLanguageType language : languages) {
            bitSet |= language.bitMask();
        }
        out.writeIntLE(bitSet);
    }

    private final int bitMask;
}
