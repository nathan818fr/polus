package com.nathan818.polus.protocol.packet;

import com.nathan818.polus.protocol.packet.game.AddPlayerPacket;
import com.nathan818.polus.protocol.packet.game.EndGamePacket;
import com.nathan818.polus.protocol.packet.game.GameActionPacket;
import com.nathan818.polus.protocol.packet.game.GameActionToTargetPacket;
import com.nathan818.polus.protocol.packet.game.GamePropertyPacket;
import com.nathan818.polus.protocol.packet.game.JoinGamePacket;
import com.nathan818.polus.protocol.packet.game.KickPlayerPacket;
import com.nathan818.polus.protocol.packet.game.RejectPlayerPacket;
import com.nathan818.polus.protocol.packet.game.RemovePlayerPacket;
import com.nathan818.polus.protocol.packet.game.SpawnPacket;
import com.nathan818.polus.protocol.packet.game.StartGamePacket;
import com.nathan818.polus.protocol.packet.game.WaitForHostPacket;
import com.nathan818.polus.protocol.packet.initial.CreateGamePacket;
import com.nathan818.polus.protocol.packet.initial.FindGamePacket;
import com.nathan818.polus.protocol.packet.initial.GameCreatedPacket;
import com.nathan818.polus.protocol.packet.initial.GameListPacket;
import com.nathan818.polus.protocol.packet.initial.LoginPacket;
import com.nathan818.polus.protocol.packet.initial.RedirectPacket;

public abstract class AbstractPacketHandler {
    public void handle(LoginPacket packet) throws Exception {
    }

    public void handle(QuitPacket packet) throws Exception {
    }

    public void handle(CreateGamePacket packet) throws Exception {
    }

    public void handle(JoinGamePacket packet) throws Exception {
    }

    public void handle(RejectPlayerPacket packet) throws Exception {
    }

    public void handle(StartGamePacket packet) throws Exception {
    }

    public void handle(EndGamePacket packet) throws Exception {
    }

    public void handle(GamePropertyPacket packet) throws Exception {
    }

    public void handle(KickPlayerPacket packet) throws Exception {
    }

    public void handle(FindGamePacket packet) throws Exception {
    }

    public void handle(DisconnectPacket packet) throws Exception {
    }

    public void handle(GameListPacket packet) throws Exception {
    }

    public void handle(GameCreatedPacket packet) throws Exception {
    }

    public void handle(RedirectPacket packet) throws Exception {
    }

    public void handle(SpawnPacket packet) throws Exception {
    }

    public void handle(AddPlayerPacket packet) throws Exception {
    }

    public void handle(RemovePlayerPacket packet) throws Exception {
    }

    public void handle(GameActionPacket packet) throws Exception {
    }

    public void handle(GameActionToTargetPacket packet) throws Exception {
    }

    public void handle(WaitForHostPacket packet) throws Exception {
    }
}
