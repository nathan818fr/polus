package com.nathan818.polus.protocol.packet.initial;

import com.nathan818.polus.protocol.packet.AbstractPacketHandler;
import com.nathan818.polus.protocol.packet.PolusPacket;
import com.nathan818.polus.protocol.packet.type.GameInfoType;
import com.nathan818.polus.protocol.packet.type.GameMapType;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class GameListPacket extends PolusPacket {
    private final Map<GameMapType, Integer> totalCount = new EnumMap<>(GameMapType.class);
    private final List<GameInfoType> games = new ArrayList<>();

    @Override
    public void read(ByteBuf in, Recipient recipient) {
        totalCount.clear();
        games.clear();
        while (in.isReadable()) {
            Message m = readMessage(in);
            switch (m.id()) {
                case 1:
                    readTotalCount(m.buf());
                    break;
                case 0:
                    readGames(m.buf());
                    break;
                default:
                    // ignore unknown (lenient)
            }
        }
    }

    private void readTotalCount(ByteBuf in) {
        for (GameMapType map : GameMapType.values()) {
            if (!in.isReadable()) {
                return;
            }
            totalCount.put(map, in.readIntLE());
        }
    }

    private void readGames(ByteBuf in) {
        while (in.isReadable()) {
            Message m = readMessage(in);
            if (m.id() != 0) {
                // ignore unknown (lenient)
                continue;
            }
            GameInfoType game = new GameInfoType();
            game.read(in);
            games.add(game);
        }
    }

    @Override
    public void write(ByteBuf out, Recipient recipient) {
        Writer w = beginMessage(out, 1);
        writeTotalCount(w.buf());
        w.end();

        w = beginMessage(out, 0);
        writeGames(w.buf());
        w.end();
    }

    private void writeTotalCount(ByteBuf out) {
        for (GameMapType map : GameMapType.values()) {
            out.writeIntLE(totalCount.getOrDefault(map, 0));
        }
    }

    private void writeGames(ByteBuf out) {
        for (GameInfoType game : games) {
            Writer w = beginMessage(out, 0);
            game.write(w.buf());
            w.end();
        }
    }

    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        handler.handle(this);
    }
}
