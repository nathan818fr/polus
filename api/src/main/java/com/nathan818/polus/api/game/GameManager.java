package com.nathan818.polus.api.game;

import java.util.Collection;

public interface GameManager {
    Collection<? extends Game> getGames();

    Game getGame(String code);
}
