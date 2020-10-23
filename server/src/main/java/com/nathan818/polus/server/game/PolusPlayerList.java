package com.nathan818.polus.server.game;

import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.api.game.Game;
import com.nathan818.polus.api.game.Player;
import com.nathan818.polus.api.game.PlayerList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolusPlayerList implements PlayerList {
    private final PolusGame game;

    private int playerIdCounter;
    private final Int2ObjectMap<PolusPlayer> playersById = new Int2ObjectOpenHashMap<>();
    private final List<PolusPlayer> players = new CopyOnWriteArrayList<>();
    private final List<PolusPlayer> playersView = Collections.unmodifiableList(players);
    private final Set<String> bannedIps = new HashSet<>();

    void addPlayer(PolusPlayerConnection connection) {
        DisconnectReason kickReason = null;
        if (game.getState() != Game.State.LOBBY) {
            kickReason = DisconnectReason.GAME_ALREADY_STARTED;
        } else if (players.size() >= game.getOptions().getMaxPlayers()) {
            kickReason = DisconnectReason.GAME_FULL;
        } else if (isIpBanned(connection.getAddress().getAddress().getHostAddress())) {
            kickReason = DisconnectReason.BANNED_FROM_GAME;
        }

        // PLANNED: EVENT
        if (kickReason != null) {
            connection.kick(kickReason);
            return;
        }

        int playerId = ++playerIdCounter;
        PolusPlayer player = new PolusPlayer(game, connection, playerId);
        connection.setPlayer(player);
        playersById.put(playerId, player);
        players.add(player);

        if (players.size() == 1) {
            // Since the first player joined no timeout is needed anymore (the game will stop when last player quit)!
            game.cancelTimeout();
        }

        game.getNetwork().notifyPlayerAdded(player);
    }

    boolean removePlayer(PolusPlayer player, DisconnectReason reason, String message) {
        if (playersById.remove(player.getId(), player)) {
            players.remove(player);

            if (players.isEmpty()) {
                // Last player quited, stop the game (delay stop execution)
                game.getExecutor().execute(() -> {
                    if (players.isEmpty()) {
                        game.remove();
                    }
                });
            }

            game.getNetwork().notifyPlayerRemoved(player, reason, message);
            return true;
        }
        return false;
    }

    @Override
    public List<PolusPlayer> getPlayers() {
        return playersView;
    }

    @Override
    public PolusPlayer getPlayer(int playerId) {
        return playersById.get(playerId);
    }

    @Override
    public boolean hasPlayer(Player player) {
        return player != null && player.equals(playersById.get(player.getId()));
    }

    public void banIp(String ip) {
        bannedIps.add(ip);
        while (bannedIps.size() > 100) {
            bannedIps.iterator().remove();
        }
    }

    public boolean isIpBanned(String ip) {
        return bannedIps.contains(ip);
    }
}
