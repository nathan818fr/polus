package com.nathan818.polus.server.limbo;

import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.protocol.packet.game.WaitForHostPacket;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.game.PolusGame;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolusLimbo {
    private final PolusServer server;
    private final int codeId;
    private final int hostId;

    private final Set<PolusLimboConnection> waitingForHost = new HashSet<>();
    private State state = State.WAITING_FOR_HOST;

    public synchronized void handleJoin(PolusLimboConnection connection) {
    }

    public synchronized void handleQuit(PolusLimboConnection connection) {
        waitingForHost.remove(connection);

        if (state != State.HOST_REJOINED && connection.getPlayerId() == hostId) {
            state = State.HOST_DISCONNECTED;
            notifyWaitingForHost();
        }
    }

    public synchronized void handleRejoin(PolusLimboConnection connection) {
        switch (state) {
            case HOST_REJOINED: {
                PolusGame game = server.getGameManager().getGame(codeId);
                if (game == null) {
                    connection.kick(DisconnectReason.GAME_NOT_FOUND);
                } else {
                    connection.gotoGame(game);
                }
                break;
            }

            case HOST_DISCONNECTED: {
                connection.kick("The host has disconnected");
                break;
            }

            case WAITING_FOR_HOST: {
                if (waitingForHost.add(connection)) {
                    if (connection.getPlayerId() == hostId) {
                        PolusGame newGame = server.getGameManager().createGame(connection, codeId, null);
                        if (newGame == null) {
                            connection.kick(DisconnectReason.GAME_NOT_FOUND);
                        } else {
                            state = State.HOST_REJOINED;
                            connection.gotoGame(newGame);
                            notifyWaitingForHost();
                        }
                        return;
                    }

                    connection.sendPacket(new WaitForHostPacket()
                            .gameCode(codeId)
                            .playerId(connection.getPlayerId()));
                }
                break;
            }
        }
    }

    private synchronized void notifyWaitingForHost() {
        new ArrayList<>(waitingForHost).forEach(connection -> connection.getExecutor().execute(() -> {
            if (connection.isActive()) {
                handleRejoin(connection);
            }
        }));
    }

    enum State {
        WAITING_FOR_HOST,
        HOST_REJOINED,
        HOST_DISCONNECTED,
    }
}
