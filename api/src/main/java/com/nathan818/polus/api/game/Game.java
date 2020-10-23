package com.nathan818.polus.api.game;

import com.nathan818.polus.api.Server;
import com.nathan818.polus.api.game.options.GameOptions;
import com.nathan818.polus.api.util.concurrent.Executor;

public interface Game {
    Server getServer();

    Executor getExecutor();

    String getCode();

    State getState();

    void remove();

    GameOptions getOptions();

    PlayerList getPlayerList();

    enum State {
        LOBBY,
        RUNNING,
        FINISHED,
        REMOVED,
    }
}
