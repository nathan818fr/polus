package com.nathan818.polus.server.game;

import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.game.AddPlayerPacket;
import com.nathan818.polus.protocol.packet.game.EndGamePacket;
import com.nathan818.polus.protocol.packet.game.GameActionPacket;
import com.nathan818.polus.protocol.packet.game.GameActionToTargetPacket;
import com.nathan818.polus.protocol.packet.game.GamePropertyPacket;
import com.nathan818.polus.protocol.packet.game.KickPlayerPacket;
import com.nathan818.polus.protocol.packet.game.RejectPlayerPacket;
import com.nathan818.polus.protocol.packet.game.RemovePlayerPacket;
import com.nathan818.polus.protocol.packet.game.SpawnPacket;
import com.nathan818.polus.protocol.packet.game.StartGamePacket;
import com.nathan818.polus.protocol.packet.game.action.GameAction;
import com.nathan818.polus.protocol.packet.type.GamePropertyType;
import com.nathan818.polus.server.connection.ProtocolMapper;
import com.nathan818.polus.server.limbo.PolusLimbo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PolusGameNetwork {
    private final PolusGame game;
    private int hostId;

    public void handleJoin(PolusPlayerConnection connection) {
        game.getPlayerList().addPlayer(connection);
    }

    public void handleQuit(PolusPlayerConnection connection) {
        PolusPlayer player = connection.getPlayer();
        if (player != null) {
            player.kick(DisconnectReason.PLAYER_QUIT);
        }
    }

    public void handleStartGame(PolusPlayer player, StartGamePacket packet) {
        if (hostId == player.getId()) {
            game.start();
        }
    }

    public void handleEndGame(PolusPlayer player, EndGamePacket packet) {
        if (hostId == player.getId()) {
            game.end(packet._field2(), packet._field3());
        }
    }

    public void handleUpdateProperty(PolusPlayer player, GamePropertyPacket packet) {
        if (hostId == player.getId()) {
            game.getOptions().setProperty(packet.property(), packet.value());
        }
    }

    public void handleRejectPlayer(PolusPlayer player, RejectPlayerPacket packet) {
        if (hostId == player.getId()) {
            PolusPlayer target = game.getPlayerList().getPlayer(packet.playerId());
            if (target != null) {
                target.kick(ProtocolMapper.asAPI(packet.reason()));
            }
        }
    }

    public void handleKickPlayer(PolusPlayer player, KickPlayerPacket packet) {
        if (hostId == player.getId()) {
            PolusPlayer target = game.getPlayerList().getPlayer(packet.playerId());
            if (target != null) {
                if (packet.ban()) {
                    game.getPlayerList().banIp(player.getConnection().getAddress().getAddress().getHostAddress());
                    target.kick(DisconnectReason.BANNED_FROM_GAME);
                } else {
                    target.kick(DisconnectReason.KICKED_FROM_GAME);
                }
            }
        }
    }

    public void handleAction(PolusPlayer player, GameActionPacket packet) {
        handleAction(player, packet.action(), -1, packet.reliable());
    }

    public void handleAction(PolusPlayer player, GameActionToTargetPacket packet) {
        handleAction(player, packet.action(), packet.targetId(), packet.reliable());
    }

    private void handleAction(PolusPlayer player, GameAction action, int targetId, boolean reliable) {
        if (targetId != -1) {
            PolusPlayer target = game.getPlayerList().getPlayer(targetId);
            if (target == null) {
                return;
            }
            target.getConnection().sendPacket(new GameActionPacket().gameCode(game.getCodeId()).action(action));
            return;
        }

        broadcastPacket(player, new GameActionPacket().gameCode(game.getCodeId()).action(action));
    }

    private void broadcastPacket(PolusPacket packet) {
        game.getPlayerList().getPlayers().forEach(o -> o.getConnection().sendPacket(packet));
    }

    private void broadcastPacket(PolusPlayer except, PolusPacket packet) {
        game.getPlayerList().getPlayers().forEach(o -> {
            if (o != except) {
                o.getConnection().sendPacket(packet);
            }
        });
    }

    private void sendAllProperties(PolusPlayerConnection connection) {
        for (GamePropertyType property : GamePropertyType.values()) {
            connection.sendPacket(new GamePropertyPacket()
                    .gameCode(game.getCodeId())
                    .property(property)
                    .value(game.getOptions().getProperty(property)));
        }
    }

    public void notifyPlayerAdded(PolusPlayer player) {
        if (hostId == 0) {
            hostId = player.getId();
        }

        sendAllProperties(player.getConnection());
        SpawnPacket spawnPacket = new SpawnPacket()
                .gameCode(game.getCodeId())
                .playerId(player.getId())
                .hostId(hostId);
        for (PolusPlayer o : game.getPlayerList().getPlayers()) {
            if (o != player) {
                spawnPacket.playersIds().add(o.getId());
            }
        }
        player.getConnection().sendPacket(spawnPacket);

        broadcastPacket(player, new AddPlayerPacket()
                .gameCode(game.getCodeId())
                .playerId(player.getId())
                .hostId(hostId));
    }

    public void notifyPlayerRemoved(PolusPlayer player, DisconnectReason reason, String message) {
        if (hostId == player.getId()) {
            game.getExecutor().execute(game::remove);
        }

        if (message != null) {
            player.getConnection().kick(message);
        } else {
            player.getConnection().kick(reason);
        }

        broadcastPacket(new RemovePlayerPacket()
                .gameCode(game.getCodeId())
                .playerId(player.getId())
                .hostId(hostId)
                .reason(ProtocolMapper.asProtocol(reason)));
    }

    public void notifyPropertyUpdated(GamePropertyType property) {
        broadcastPacket(new GamePropertyPacket()
                .gameCode(game.getCodeId())
                .property(property)
                .value(game.getOptions().getProperty(property)));
    }

    public void notifyOptionsUpdated() {
        // NTD
    }

    public void notifyGameStart() {
        broadcastPacket(new StartGamePacket().gameCode(game.getCodeId()));
    }

    public void notifyGameEnd(int field2, int field3) {
        PolusLimbo limbo = new PolusLimbo(game.getServer(), game.getCodeId(), hostId);
        EndGamePacket endPacket = new EndGamePacket()
                .gameCode(game.getCodeId())
                ._field2(field2)
                ._field3(field3);
        for (PolusPlayer o : game.getPlayerList().getPlayers()) {
            o.getConnection().gotoLimbo(endPacket, limbo);
        }
        for (PolusPlayer o : game.getPlayerList().getPlayers()) {
            o.kick(DisconnectReason.PLAYER_QUIT);
        }
    }
}
