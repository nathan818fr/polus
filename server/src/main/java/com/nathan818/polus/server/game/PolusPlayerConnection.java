package com.nathan818.polus.server.game;

import com.nathan818.polus.protocol.packet.game.EndGamePacket;
import com.nathan818.polus.server.connection.PolusConnection;
import com.nathan818.polus.server.limbo.PolusLimbo;

public interface PolusPlayerConnection extends PolusConnection {
    void setPlayer(PolusPlayer player);

    PolusPlayer getPlayer();

    void gotoLimbo(EndGamePacket endPacket, PolusLimbo limbo);
}
