package com.nathan818.polus.api.game;

import java.util.List;

public interface PlayerList {
    List<? extends Player> getPlayers();

    Player getPlayer(int playerId);

    boolean hasPlayer(Player player);
}
