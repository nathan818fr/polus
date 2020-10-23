package com.nathan818.polus.server.limbo;

import com.nathan818.polus.server.connection.PolusConnection;
import com.nathan818.polus.server.game.PolusGame;

public interface PolusLimboConnection extends PolusConnection {
    int getPlayerId();

    void gotoGame(PolusGame game);
}
