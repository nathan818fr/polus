package com.nathan818.polus.api;

import com.nathan818.polus.api.game.GameManager;

public interface Server {
    boolean isRunning();

    void stop();

    GameManager getGameManager();
}
