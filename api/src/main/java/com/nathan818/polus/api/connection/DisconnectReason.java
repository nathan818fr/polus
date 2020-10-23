package com.nathan818.polus.api.connection;

public enum DisconnectReason {
    UNKNOWN,
    PLAYER_QUIT,
    GAME_FULL,
    GAME_ALREADY_STARTED,
    GAME_NOT_FOUND,
    OUTDATED_CLIENT,
    BANNED_FROM_GAME,
    KICKED_FROM_GAME,
    INVALID_PLAYERNAME,
    BANNED_FOR_HACKING,
    GAME_CLOSED_BY_SERVER,
    SERVER_FULL,
    ;
}
