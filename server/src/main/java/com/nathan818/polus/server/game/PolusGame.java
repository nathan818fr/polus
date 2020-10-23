package com.nathan818.polus.server.game;

import com.nathan818.polus.api.game.Game;
import com.nathan818.polus.api.util.concurrent.Executor;
import com.nathan818.polus.protocol.util.GameCodeUtil;
import com.nathan818.polus.server.PolusServer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Getter
public class PolusGame implements Game {
    private static final Logger logger = LoggerFactory.getLogger(PolusGame.class);

    private final PolusGameManager manager;
    private final Executor executor;
    private final int codeId;
    private final String code; // < cached string representation of codeId
    private final String hostName;

    private ScheduledFuture<?> timeoutTask;
    private volatile State state = State.LOBBY;

    private final PolusGameNetwork network = new PolusGameNetwork(this);
    private final PolusGameOptions options = new PolusGameOptions(this);
    private final PolusPlayerList playerList = new PolusPlayerList(this);

    public PolusGame(PolusGameManager manager, Executor executor, int codeId, String hostName) {
        this.manager = manager;
        this.executor = executor;
        this.codeId = codeId;
        this.code = GameCodeUtil.gameCodeToString(codeId);
        this.hostName = hostName;
    }

    @Override
    public PolusServer getServer() {
        return manager.getServer();
    }

    void created() {
        timeoutTask = executor.schedule(this::remove, 10, TimeUnit.SECONDS);
    }

    void cancelTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
    }

    void start() {
        if (state != State.REMOVED) {
            state = State.RUNNING;
            getNetwork().notifyGameStart();
        }
    }

    void end(int field2, int field3) {
        if (state != State.REMOVED) {
            state = State.FINISHED;
            getNetwork().notifyGameEnd(field2, field3);
        }
    }

    public void remove() {
        if (state != State.REMOVED) {
            state = State.REMOVED;
            try {
                cancelTimeout();

                for (PolusPlayer player : getPlayerList().getPlayers()) {
                    player.kick("Game was removed");
                }
            } finally {
                manager.removeGame(this);
            }
        }
    }
}
