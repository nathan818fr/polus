package com.nathan818.polus.protocol.packet.type;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@NoArgsConstructor
@Data
public class GameOptionsType {
    private static final int LATEST_VERSION = 4;

    private int version = LATEST_VERSION;
    private int maxPlayers;
    private final Set<ChatLanguageType> languages = EnumSet.noneOf(ChatLanguageType.class);
    private final Set<GameMapType> maps = EnumSet.noneOf(GameMapType.class);
    private float playerSpeed;
    private float crewmateVision;
    private float impostorVision;
    private int killCooldown;
    private int tasksCountCommon;
    private int tasksCountLong;
    private int tasksCountShort;
    private int emergencyCount;
    private int impostorsCount;
    private @NonNull KillDistanceType killDistance = KillDistanceType.MEDIUM;
    private int discussionTime;
    private int votingTime;
    private boolean isRecommended;
    private int emergencyCooldown;
    private boolean confirmEjects;
    private boolean visualTasks;
    private boolean anonymousVotes;
    private @NonNull TaskbarUpdateType taskbarUpdates = TaskbarUpdateType.ALWAYS;

    public GameOptionsType language(ChatLanguageType language) {
        Objects.requireNonNull(language);
        languages.clear();
        languages.add(language);
        return this;
    }

    public ChatLanguageType language() {
        for (ChatLanguageType language : languages) {
            return language;
        }
        return ChatLanguageType.ENGLISH;
    }

    public GameOptionsType map(GameMapType map) {
        Objects.requireNonNull(map);
        maps.clear();
        maps.add(map);
        return this;
    }

    public GameMapType map() {
        for (GameMapType map : maps) {
            return map;
        }
        return GameMapType.POLUS;
    }

    public void read(ByteBuf in, boolean multipleMaps) {
        version = in.readUnsignedByte();

        maxPlayers = in.readUnsignedByte();
        ChatLanguageType.readSet(in, languages);
        if (multipleMaps) {
            GameMapType.readSet(in, maps);
        } else {
            map(GameMapType.read(in));
        }
        playerSpeed = in.readFloatLE();
        crewmateVision = in.readFloatLE();
        impostorVision = in.readFloatLE();
        killCooldown = (int) in.readFloatLE();
        tasksCountCommon = in.readUnsignedByte();
        tasksCountLong = in.readUnsignedByte();
        tasksCountShort = in.readUnsignedByte();
        emergencyCount = in.readIntLE();
        impostorsCount = in.readUnsignedByte();
        killDistance = KillDistanceType.read(in);
        discussionTime = in.readIntLE();
        votingTime = in.readIntLE();
        isRecommended = in.readBoolean();

        if (version > 1) {
            emergencyCooldown = in.readUnsignedByte();
        }

        if (version > 2) {
            confirmEjects = in.readBoolean();
            visualTasks = in.readBoolean();
        }

        if (version > 3) {
            anonymousVotes = in.readBoolean();
            taskbarUpdates = TaskbarUpdateType.read(in);
        }
    }

    public void write(ByteBuf out, boolean multipleMaps) {
        out.writeByte(version);

        out.writeByte(maxPlayers);
        ChatLanguageType.writeSet(out, languages);
        if (multipleMaps) {
            GameMapType.writeSet(out, maps);
        } else {
            GameMapType.write(out, map());
        }
        out.writeFloatLE(playerSpeed);
        out.writeFloatLE(crewmateVision);
        out.writeFloatLE(impostorVision);
        out.writeFloatLE(killCooldown);
        out.writeByte(tasksCountCommon);
        out.writeByte(tasksCountLong);
        out.writeByte(tasksCountShort);
        out.writeIntLE(emergencyCount);
        out.writeByte(impostorsCount);
        KillDistanceType.write(out, killDistance);
        out.writeIntLE(discussionTime);
        out.writeIntLE(votingTime);
        out.writeBoolean(isRecommended);

        if (version > 1) {
            out.writeByte(emergencyCooldown);
        }

        if (version > 2) {
            out.writeBoolean(confirmEjects);
            out.writeBoolean(visualTasks);
        }

        if (version > 3) {
            out.writeBoolean(anonymousVotes);
            TaskbarUpdateType.write(out, taskbarUpdates);
        }
    }
}
