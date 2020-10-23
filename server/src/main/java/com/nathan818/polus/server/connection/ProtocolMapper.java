package com.nathan818.polus.server.connection;

import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.api.game.options.ChatLanguage;
import com.nathan818.polus.api.game.options.GameMap;
import com.nathan818.polus.api.game.options.KillDistance;
import com.nathan818.polus.api.game.options.TaskbarUpdate;
import com.nathan818.polus.protocol.packet.type.ChatLanguageType;
import com.nathan818.polus.protocol.packet.type.DisconnectReasonType;
import com.nathan818.polus.protocol.packet.type.GameMapType;
import com.nathan818.polus.protocol.packet.type.KillDistanceType;
import com.nathan818.polus.protocol.packet.type.TaskbarUpdateType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolMapper {
    // TODO: Unit tests

    public static ChatLanguage asAPI(ChatLanguageType language) {
        return language == null ? null : ChatLanguage.values()[language.ordinal()];
    }

    public static ChatLanguageType asProtocol(ChatLanguage language) {
        return language == null ? null : ChatLanguageType.values()[language.ordinal()];
    }

    public static GameMap asAPI(GameMapType map) {
        return map == null ? null : GameMap.values()[map.ordinal()];
    }

    public static GameMapType asProtocol(GameMap map) {
        return map == null ? null : GameMapType.values()[map.ordinal()];
    }

    public static KillDistance asAPI(KillDistanceType killDistance) {
        return killDistance == null ? null : KillDistance.values()[killDistance.ordinal()];
    }

    public static KillDistanceType asProtocol(KillDistance killDistance) {
        return killDistance == null ? null : KillDistanceType.values()[killDistance.ordinal()];
    }

    public static TaskbarUpdate asAPI(TaskbarUpdateType taskbarUpdates) {
        return taskbarUpdates == null ? null : TaskbarUpdate.values()[taskbarUpdates.ordinal()];
    }

    public static TaskbarUpdateType asProtocol(TaskbarUpdate taskbarUpdates) {
        return taskbarUpdates == null ? null : TaskbarUpdateType.values()[taskbarUpdates.ordinal()];
    }

    public static DisconnectReason asAPI(DisconnectReasonType reason) {
        try {
            return reason == null ? null : DisconnectReason.valueOf(reason.name());
        } catch (IllegalArgumentException e) {
            return DisconnectReason.UNKNOWN;
        }
    }

    public static DisconnectReasonType asProtocol(DisconnectReason reason) {
        try {
            return reason == null ? null : DisconnectReasonType.valueOf(reason.name());
        } catch (IllegalArgumentException e) {
            return DisconnectReasonType.PLAYER_QUIT;
        }
    }
}
