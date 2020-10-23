package com.nathan818.polus.server.game;

import com.nathan818.polus.api.game.options.ChatLanguage;
import com.nathan818.polus.api.game.options.GameMap;
import com.nathan818.polus.api.game.options.GameOptions;
import com.nathan818.polus.api.game.options.KillDistance;
import com.nathan818.polus.api.game.options.TaskCategory;
import com.nathan818.polus.api.game.options.TaskbarUpdate;
import com.nathan818.polus.protocol.packet.type.GameOptionsType;
import com.nathan818.polus.protocol.packet.type.GamePropertyType;
import com.nathan818.polus.server.connection.ProtocolMapper;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@Getter
public class PolusGameOptions implements GameOptions {
    private final PolusGame game;

    private boolean isPublic = false;
    private int maxPlayers = 10;
    private ChatLanguage language = ChatLanguage.ENGLISH;
    private GameMap map = GameMap.POLUS;
    private int impostorsCount = 2;
    private double playerSpeed = 1.0D;
    private double crewmateVision = 1.0D;
    private double impostorVision = 1.5D;
    private int killCooldown = 45;
    private KillDistance killDistance = KillDistance.MEDIUM;
    private final Map<TaskCategory, Integer> tasksCount = new EnumMap<>(TaskCategory.class);
    private int emergencyCount = 1;
    private int emergencyCooldown = 15;
    private boolean confirmEjects = true;
    private boolean visualTasks = true;
    private boolean anonymousVotes = false;
    private TaskbarUpdate taskbarUpdates = TaskbarUpdate.ALWAYS;

    {
        tasksCount.put(TaskCategory.COMMON, 1);
        tasksCount.put(TaskCategory.LONG, 1);
        tasksCount.put(TaskCategory.SHORT, 2);
    }

    void setOptions(GameOptionsType options) {
        setMaxPlayers(options.maxPlayers());
        setLanguage(ProtocolMapper.asAPI(options.language()));
        setMap(ProtocolMapper.asAPI(options.map()));
        setImpostorsCount(options.impostorsCount());
        setPlayerSpeed(options.playerSpeed());
        setCrewmateVision(options.crewmateVision());
        setImpostorVision(options.impostorVision());
        setKillCooldown(options.killCooldown());
        setKillDistance(ProtocolMapper.asAPI(options.killDistance()));
        setTasksCount(TaskCategory.COMMON, options.tasksCountCommon());
        setTasksCount(TaskCategory.LONG, options.tasksCountLong());
        setTasksCount(TaskCategory.SHORT, options.tasksCountShort());
        setEmergencyCount(options.emergencyCount());
        setEmergencyCooldown(options.emergencyCooldown());
        setConfirmEjects(options.confirmEjects());
        setVisualTasks(options.visualTasks());
        setAnonymousVotes(options.anonymousVotes());
        setTaskbarUpdates(ProtocolMapper.asAPI(options.taskbarUpdates()));
    }

    void setProperty(GamePropertyType property, boolean value) {
        switch (property) {
            case IS_PUBLIC:
                setPublic(value);
                break;
        }
    }

    boolean getProperty(GamePropertyType property) {
        switch (property) {
            case IS_PUBLIC:
                return isPublic();

            default:
                return false;
        }
    }

    public void setPublic(boolean isPublic) {
        if (this.isPublic != isPublic) {
            this.isPublic = isPublic;
            game.getNetwork().notifyPropertyUpdated(GamePropertyType.IS_PUBLIC);
        }
    }

    public void setMaxPlayers(int maxPlayers) {
        if (this.maxPlayers != maxPlayers) {
            this.maxPlayers = maxPlayers;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setLanguage(ChatLanguage language) {
        if (this.language != language) {
            this.language = requireNonNull(language);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setMap(GameMap map) {
        if (this.map != map) {
            this.map = requireNonNull(map);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    private void setImpostorsCount(int impostorsCount) {
        if (this.impostorsCount != impostorsCount) {
            this.impostorsCount = impostorsCount;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setPlayerSpeed(double playerSpeed) {
        if (Double.compare(this.playerSpeed, playerSpeed) != 0) {
            this.playerSpeed = requireFinite(playerSpeed);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setCrewmateVision(double crewmateVision) {
        if (Double.compare(this.crewmateVision, crewmateVision) != 0) {
            this.crewmateVision = requireFinite(crewmateVision);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setImpostorVision(double impostorVision) {
        if (Double.compare(this.impostorVision, impostorVision) != 0) {
            this.impostorVision = requireFinite(impostorVision);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setKillCooldown(int killCooldown) {
        if (this.killCooldown != killCooldown) {
            this.killCooldown = killCooldown;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setKillDistance(KillDistance killDistance) {
        if (this.killDistance != killDistance) {
            this.killDistance = requireNonNull(killDistance);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setTasksCount(TaskCategory category, int count) {
        Objects.requireNonNull(category);
        if (tasksCount.get(category) != count) {
            tasksCount.put(category, count);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setEmergencyCount(int emergencyCount) {
        if (this.emergencyCount != emergencyCount) {
            this.emergencyCount = emergencyCount;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setEmergencyCooldown(int emergencyCooldown) {
        if (this.emergencyCooldown != emergencyCooldown) {
            this.emergencyCooldown = emergencyCooldown;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setConfirmEjects(boolean confirmEjects) {
        if (this.confirmEjects != confirmEjects) {
            this.confirmEjects = confirmEjects;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setVisualTasks(boolean visualTasks) {
        if (this.visualTasks != visualTasks) {
            this.visualTasks = visualTasks;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setAnonymousVotes(boolean anonymousVotes) {
        if (this.anonymousVotes != anonymousVotes) {
            this.anonymousVotes = anonymousVotes;
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    public void setTaskbarUpdates(TaskbarUpdate taskbarUpdates) {
        if (this.taskbarUpdates != taskbarUpdates) {
            this.taskbarUpdates = requireNonNull(taskbarUpdates);
            game.getNetwork().notifyOptionsUpdated();
        }
    }

    private static double requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(value + " is not a finite number");
        }
        return value;
    }
}
