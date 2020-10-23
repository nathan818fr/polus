package com.nathan818.polus.server.game;

import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.api.game.Player;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PolusPlayer implements Player {
    private final PolusGame game;
    private final PolusPlayerConnection connection;
    private final int id;

    public void kick(String message) {
        kick(DisconnectReason.KICKED_FROM_GAME, message);
    }

    public void kick(DisconnectReason reason) {
        kick(reason, null);
    }

    private void kick(DisconnectReason reason, String message) {
        game.getPlayerList().removePlayer(this, Objects.requireNonNull(reason), message);
    }
}
