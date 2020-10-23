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
public enum DisconnectReasonType {
    // -
    PLAYER_QUIT(0),

    // The game you tried to join is full.
    // Check with the host to see if you can join next round.
    GAME_FULL(1),

    // The game you tried to join is already started.
    // Check with the host to see if you can join next round.
    GAME_ALREADY_STARTED(2),

    // Could not find the game you're looking for.
    GAME_NOT_FOUND(3),

    // You are running an older version of the game.
    // Please update to play with others.
    OUTDATED_CLIENT(5),

    // You were banned from the room.
    // You cannot rejoin that room.
    BANNED_FROM_GAME(6),

    // You were kicked from the room.
    // You can rejoin if the room hasn't started.
    KICKED_FROM_GAME(7),

    // {customMessage}
    CUSTOM(8),

    // Server refused username: {playerName}
    INVALID_PLAYERNAME(9),

    //
    BANNED_FOR_HACKING(10),

    // (no messages)
    SILENT(16),

    // You disconnected from the server.
    // If this happens often, check your network strength.
    // This may also be a server issue.
    NETWORK_ISSUES(17),

    // The server stopped this game. Possibly due to inactivity.
    GAME_CLOSED_BY_SERVER(19),

    // The Among Us servers are overloaded.
    // Sorry! Please try again later!
    SERVER_FULL(20),
    ;

    private static final Int2ObjectMap<DisconnectReasonType> BY_ID;

    static {
        BY_ID = new Int2ObjectOpenHashMap<>(values().length, 0.25F);
        for (DisconnectReasonType reason : values()) {
            BY_ID.put(reason.id(), reason);
        }
    }

    public static DisconnectReasonType byId(int id) {
        return BY_ID.get(id);
    }

    public static DisconnectReasonType read(ByteBuf in) {
        int id = in.readUnsignedByte();
        DisconnectReasonType reason = byId(id);
        if (reason == null) {
            throw new UnknownInputException("DisconnectReason", id);
        }
        return reason;
    }

    public static void write(ByteBuf out, DisconnectReasonType reason) {
        out.writeByte(reason.id());
    }

    private final int id;

    public String getMessage() {
        return name(); // TODO
    }
}
