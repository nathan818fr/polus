package com.nathan818.polus.server.connection.handler;

import com.google.common.base.Preconditions;
import com.nathan818.polus.protocol.exception.IllegalProtocolStateException;
import com.nathan818.polus.protocol.packet.game.EndGamePacket;
import com.nathan818.polus.protocol.packet.game.GameActionPacket;
import com.nathan818.polus.protocol.packet.game.GameActionToTargetPacket;
import com.nathan818.polus.protocol.packet.game.GamePacket;
import com.nathan818.polus.protocol.packet.game.GamePropertyPacket;
import com.nathan818.polus.protocol.packet.game.KickPlayerPacket;
import com.nathan818.polus.protocol.packet.game.RejectPlayerPacket;
import com.nathan818.polus.protocol.packet.game.StartGamePacket;
import com.nathan818.polus.server.game.PolusGame;
import com.nathan818.polus.server.game.PolusGameNetwork;
import com.nathan818.polus.server.game.PolusPlayer;
import com.nathan818.polus.server.game.PolusPlayerConnection;
import com.nathan818.polus.server.limbo.PolusLimbo;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class GameHandler extends PacketHandler implements PolusPlayerConnection {
    private static final Logger logger = LoggerFactory.getLogger(GameHandler.class);

    private final PolusGame game;
    private int badGameCount;
    private volatile @Getter PolusPlayer player; // set on game executor

    @Override
    public void setPlayer(PolusPlayer player) {
        assertInGameExecutor();
        Preconditions.checkState(this.player == null);
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public void enabled(boolean onConnect) {
        super.enabled(onConnect);
        game.getExecutor().execute(() -> {
            try {
                game.getNetwork().handleJoin(this);
            } catch (Throwable err) {
                kick(err);
            }
        });
    }

    @Override
    public void disabled(boolean onDisconnect) {
        super.disabled(onDisconnect);
        game.getExecutor().execute(() -> game.getNetwork().handleQuit(this));
    }

    private <T extends GamePacket> void handle(T packet, HandleFunction<T> fct) {
        assertInExecutor();
        if (packet.gameCode() == 0) {
            return;
        }
        if (packet.gameCode() != game.getCodeId()) {
            if (++badGameCount > 10) {
                throw new IllegalProtocolStateException("Invalid game (excepted: " + game.getCodeId() + ", received: " + packet.gameCode() + ")");
            }
            return;
        }
        if (player != null) {
            game.getExecutor().execute(() -> {
                if (player != null) {
                    fct.call(game.getNetwork(), player, packet);
                }
            });
        }
    }

    @Override
    public void handle(StartGamePacket packet) {
        handle(packet, PolusGameNetwork::handleStartGame);
    }

    @Override
    public void handle(EndGamePacket packet) {
        handle(packet, PolusGameNetwork::handleEndGame);
    }

    @Override
    public void handle(GamePropertyPacket packet) {
        handle(packet, PolusGameNetwork::handleUpdateProperty);
    }

    @Override
    public void handle(RejectPlayerPacket packet) throws Exception {
        handle(packet, PolusGameNetwork::handleRejectPlayer);
    }

    @Override
    public void handle(KickPlayerPacket packet) {
        handle(packet, PolusGameNetwork::handleKickPlayer);
    }

    @Override
    public void handle(GameActionPacket packet) {
        handle(packet, PolusGameNetwork::handleAction);
    }

    @Override
    public void handle(GameActionToTargetPacket packet) {
        handle(packet, PolusGameNetwork::handleAction);
    }

    @Override
    public void gotoLimbo(EndGamePacket packet, PolusLimbo limbo) {
        inExecutorIfActive(() -> {
            sendPacket(packet);
            replace(new LimboHandler(limbo, player.getId()));
        });
    }

    private void assertInGameExecutor() {
        Preconditions.checkState(game.getExecutor().inExecutor(), "Called outside the game executor");
    }

    public interface HandleFunction<T extends GamePacket> {
        void call(PolusGameNetwork game, PolusPlayer player, T packet);
    }
}
