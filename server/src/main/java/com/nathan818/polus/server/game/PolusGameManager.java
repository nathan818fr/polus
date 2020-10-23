package com.nathan818.polus.server.game;

import com.nathan818.polus.api.game.GameManager;
import com.nathan818.polus.protocol.packet.type.GameOptionsType;
import com.nathan818.polus.protocol.util.GameCodeUtil;
import com.nathan818.polus.server.PolusServer;
import com.nathan818.polus.server.connection.PolusConnection;
import com.nathan818.polus.server.netty.EventLoopExecutor;
import io.netty.channel.EventLoopGroup;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolusGameManager implements GameManager {
    private final @Getter PolusServer server;
    private final EventLoopGroup eventLoops;
    private final Map<Integer, PolusGame> games = new ConcurrentHashMap<>();
    private final Collection<PolusGame> gamesValues = Collections.unmodifiableCollection(games.values());

    public PolusGame createGame(PolusConnection host, int gameCodeId, GameOptionsType options) {
        PolusGame game = new PolusGame(this, new EventLoopExecutor(eventLoops.next()), gameCodeId, host.getName());
        if (options != null) {
            game.getOptions().setOptions(options);
        }
        if (!host.isActive() || games.putIfAbsent(game.getCodeId(), game) != null) {
            return null;
        }
        game.getExecutor().execute(game::created);
        return game;
    }

    boolean removeGame(PolusGame game) {
        return games.remove(game.getCodeId(), game);
    }

    @Override
    public Collection<PolusGame> getGames() {
        return gamesValues;
    }

    public PolusGame getGame(int code) {
        return games.get(code);
    }

    @Override
    public PolusGame getGame(String code) {
        int codeId;
        try {
            codeId = GameCodeUtil.gameCodeFromString(code);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return getGame(codeId);
    }
}
