package com.nathan818.polus.protocol.packet.type;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

import static com.nathan818.polus.protocol.packet.PolusPacket.readString;
import static com.nathan818.polus.protocol.packet.PolusPacket.readVarInt;
import static com.nathan818.polus.protocol.packet.PolusPacket.writeString;
import static com.nathan818.polus.protocol.packet.PolusPacket.writeVarInt;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
public class GameInfoType {
    private static final byte[] EMPTY_IP = new byte[4];

    private final ServerAddrType serverAddr = new ServerAddrType();
    private int gameCode;
    private @NonNull String hostName = "";
    private int playersCount;
    private int age;
    private @NonNull GameMapType map = GameMapType.THE_SKELD;
    private int impostorsCount;
    private int maxPlayers;

    public void read(ByteBuf in) {
        serverAddr.read(in);
        gameCode = in.readIntLE();
        hostName = readString(in);
        playersCount = in.readUnsignedByte();
        age = readVarInt(in);
        map = GameMapType.read(in);
        impostorsCount = in.readUnsignedByte();
        maxPlayers = in.readUnsignedByte();
    }

    public void write(ByteBuf out) {
        serverAddr.write(out);
        out.writeIntLE(gameCode);
        writeString(out, hostName);
        out.writeByte(playersCount);
        writeVarInt(out, age);
        GameMapType.write(out, map);
        out.writeByte(impostorsCount);
        out.writeByte(maxPlayers);
    }
}
