package com.nathan818.polus.server.connection.handler;

import com.nathan818.polus.protocol.packet.game.JoinGamePacket;
import com.nathan818.polus.server.game.PolusGame;
import com.nathan818.polus.server.limbo.PolusLimbo;
import com.nathan818.polus.server.limbo.PolusLimboConnection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@Getter
public class LimboHandler extends PacketHandler implements PolusLimboConnection {
    private static final Logger logger = LoggerFactory.getLogger(LimboHandler.class);

    private final PolusLimbo limbo;
    private final int playerId;

    @Override
    public void enabled(boolean onConnect) {
        super.enabled(onConnect);
        limbo.handleJoin(this);
    }

    @Override
    public void disabled(boolean onDisconnect) {
        super.disabled(onDisconnect);
        limbo.handleQuit(this);
    }

    @Override
    public void handle(JoinGamePacket packet) {
        assertInExecutor();
        limbo.handleRejoin(this);
    }

    @Override
    public void gotoGame(PolusGame game) {
        inExecutorIfActive(() -> replace(new GameHandler(game)));
    }
}
