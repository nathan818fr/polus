package com.nathan818.polus.protocol;

import com.nathan818.polus.protocol.packet.PolusPacket;
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
import com.nathan818.polus.protocol.packet.initial.RedirectPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public final class PolusProtocol {
    private static final PacketBag TO_SERVER = new PacketBag("TO_SERVER");
    private static final PacketBag TO_CLIENT = new PacketBag("TO_CLIENT");

    static {
        TO_SERVER.registerPacket(0x00, CreateGamePacket::new);
        TO_SERVER.registerPacket(0x01, JoinGamePacket::new);
        TO_SERVER.registerPacket(0x02, StartGamePacket::new);
        TO_SERVER.registerPacket(0x04, RejectPlayerPacket::new);
        TO_SERVER.registerPacket(0x05, GameActionPacket::new);
        TO_SERVER.registerPacket(0x06, GameActionToTargetPacket::new);
        TO_SERVER.registerPacket(0x08, EndGamePacket::new);
        TO_SERVER.registerPacket(0x0A, GamePropertyPacket::new);
        TO_SERVER.registerPacket(0x0B, KickPlayerPacket::new);
        TO_SERVER.registerPacket(0x10, FindGamePacket::new);

        TO_CLIENT.registerPacket(0x00, GameCreatedPacket::new);
        TO_CLIENT.registerPacket(0x01, AddPlayerPacket::new);
        TO_CLIENT.registerPacket(0x02, StartGamePacket::new);
        TO_CLIENT.registerPacket(0x04, RemovePlayerPacket::new);
        TO_CLIENT.registerPacket(0x05, GameActionPacket::new);
        TO_CLIENT.registerPacket(0x06, GameActionToTargetPacket::new);
        TO_CLIENT.registerPacket(0x07, SpawnPacket::new);
        TO_CLIENT.registerPacket(0x08, EndGamePacket::new);
        TO_CLIENT.registerPacket(0x0A, GamePropertyPacket::new);
        TO_CLIENT.registerPacket(0x0D, RedirectPacket::new);
        TO_CLIENT.registerPacket(0x0C, WaitForHostPacket::new);
        TO_CLIENT.registerPacket(0x10, GameListPacket::new);
    }

    public static PacketBag get(boolean toServer) {
        return toServer ? TO_SERVER : TO_CLIENT;
    }

    @RequiredArgsConstructor
    public static final class PacketBag {
        private final String name;
        private final Map<Integer, PacketData> packetsById = new HashMap<>();
        private final Map<Class<? extends PolusPacket>, PacketData> packetsByClass = new HashMap<>();

        private void registerPacket(int id, Supplier<PolusPacket> constructor) {
            Class<? extends PolusPacket> clazz = constructor.get().getClass();
            PacketData packetData = new PacketData(id, clazz, constructor);
            packetsById.put(id, packetData);
            packetsByClass.put(clazz, packetData);
        }

        private PacketData getPacketData(int packetId, boolean allowNull) {
            PacketData packetData = packetsById.get(packetId);
            if (packetData == null && !allowNull) {
                throw new IllegalArgumentException("Invalid or unknown packet: " + name + "/" + toHexString(packetId));
            }
            return packetData;
        }

        private PacketData getPacketData(Class<? extends PolusPacket> packetClass, boolean allowNull) {
            PacketData packetData = packetsByClass.get(packetClass);
            if (packetData == null && !allowNull) {
                throw new IllegalArgumentException(
                        "Invalid or unknown packet: " + name + "/" + packetClass.getSimpleName());
            }
            return packetData;
        }

        public PolusPacket createPacket(int packetId) {
            return getPacketData(packetId, false).getConstructor().get();
        }

        public PolusPacket createPacketOrNull(int packetId) {
            PacketData packetData = getPacketData(packetId, true);
            return packetData == null ? null : packetData.getConstructor().get();
        }

        public int getPacketId(Class<? extends PolusPacket> packetClass) {
            return getPacketData(packetClass, false).getId();
        }

        public int getPacketIdOrNegative(Class<? extends PolusPacket> packetClass) {
            PacketData packetData = getPacketData(packetClass, true);
            return packetData == null ? -1 : packetData.getId();
        }
    }

    @RequiredArgsConstructor
    @Data
    private static final class PacketData {
        private final int id;
        private final Class<? extends PolusPacket> clazz;
        private final Supplier<PolusPacket> constructor;
    }

    private static String toHexString(int packetId) {
        return (packetId < 0x10 ? "0x0" : "0x") + Integer.toHexString(packetId);
    }
}
