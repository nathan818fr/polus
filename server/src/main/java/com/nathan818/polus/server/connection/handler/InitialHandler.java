package com.nathan818.polus.server.connection.handler;

import com.google.common.collect.Iterables;
import com.nathan818.polus.api.connection.DisconnectReason;
import com.nathan818.polus.api.game.Game;
import com.nathan818.polus.api.game.options.ChatLanguage;
import com.nathan818.polus.api.game.options.GameMap;
import com.nathan818.polus.protocol.packet.game.JoinGamePacket;
import com.nathan818.polus.protocol.packet.initial.CreateGamePacket;
import com.nathan818.polus.protocol.packet.initial.FindGamePacket;
import com.nathan818.polus.protocol.packet.initial.GameCreatedPacket;
import com.nathan818.polus.protocol.packet.initial.GameListPacket;
import com.nathan818.polus.protocol.packet.initial.LoginPacket;
import com.nathan818.polus.protocol.packet.type.GameInfoType;
import com.nathan818.polus.protocol.packet.type.GameOptionsType;
import com.nathan818.polus.protocol.util.GameCodeUtil;
import com.nathan818.polus.protocol.util.GameVersionUtil;
import com.nathan818.polus.server.connection.ProtocolMapper;
import com.nathan818.polus.server.game.PolusGame;
import com.nathan818.polus.server.game.PolusGameOptions;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static com.nathan818.polus.protocol.util.ProtocolPreconditions.checkState;

@Slf4j
public class InitialHandler extends PacketHandler {
    private static final String PLAYERNAME_CHARS = "a-zA-Z0-9"; // TODO: check all allowed chars (accents, etc)
    private static final Pattern PLAYERNAME_PATTERN = Pattern.compile(
            "^(?!.{11,})[" + PLAYERNAME_CHARS + "]+(?: [" + PLAYERNAME_CHARS + "]+)* ?$");

    private ScheduledFuture<?> timeoutTask;
    private LoginState loginState = LoginState.NONE;
    private InitialState initialState = InitialState.NONE;
    private int pendingGameCode;
    private GameOptionsType pendingOptions;

    @Override
    public void enabled(boolean onConnect) {
        super.enabled(onConnect);
        timeoutTask = getExecutor().schedule(
                () -> kick("Client took too long to login"),
                5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disabled(boolean onDisconnect) {
        super.disabled(onDisconnect);
        cancelTimeoutTask();
    }

    private void cancelTimeoutTask() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);
            timeoutTask = null;
        }
    }

    @Override
    public void handle(LoginPacket packet) {
        checkState("Not expecting LoginPacket", loginState == LoginState.NONE);
        loginState = LoginState.PENDING; // PLANNED: set in PENDING state since we will add async events here

        String playerName = packet.playerName();
        int protocolVersion = packet.protocolVersion();
        if (!PLAYERNAME_PATTERN.matcher(packet.playerName()).matches()) {
            kick(DisconnectReason.INVALID_PLAYERNAME);
            return;
        }

        setName(playerName);
        setProtocolVersion(protocolVersion);
        loginState = LoginState.LOGGED;
        cancelTimeoutTask();

        log.info(this + " - Logged with version "
                + GameVersionUtil.gameVersionToString(protocolVersion) + " (" + protocolVersion + ")");
    }

    @Override
    public void handle(CreateGamePacket packet) {
        checkState("Not expecting CreateGamePacket", loginState == LoginState.LOGGED);

        if (initialState == InitialState.NONE) {
            initialState = InitialState.CREATE_GAME;
        }
        checkState("Not expecting CreateGamePacket", initialState == InitialState.CREATE_GAME);

        // PLANNED: Redirecting to others game servers will be handled by plugins.
        //   eg. They will call a method like Connection.connectTo(InetSocketAddress).
        //       This way anybody will be able to implement their own cloud systems.

        int gameCode = GameCodeUtil.randomGameCode();
        // gameCode = GameCodeUtil.gameCodeFromString("AAAAAA"); // DEBUG
        // PLANNED: EVENT (get connection, get/set gameCode, get/set options) + validate gameCode if updated?

        pendingGameCode = gameCode;
        pendingOptions = packet.options();
        sendPacket(new GameCreatedPacket().gameCode(gameCode));
        // wait for JoinGamePacket to really create the game
    }

    @Override
    public void handle(JoinGamePacket packet) {
        checkState("Not expecting JoinGamePacket", loginState == LoginState.LOGGED);

        if (initialState == InitialState.NONE) {
            initialState = InitialState.PRIVATE_GAME;
        }
        // allowed with any initialState

        PolusGame game;
        if (pendingGameCode != 0 && pendingGameCode == packet.gameCode()) {
            game = getServer().getGameManager().createGame(this, pendingGameCode, pendingOptions);
        } else {
            game = getServer().getGameManager().getGame(packet.gameCode());
        }
        if (game == null) {
            kick(DisconnectReason.GAME_NOT_FOUND);
            return;
        }
        replace(new GameHandler(game));
    }

    @Override
    public void handle(FindGamePacket packet) {
        checkState("Not expecting FindGamePacket", loginState == LoginState.LOGGED);

        if (initialState == InitialState.NONE) {
            initialState = InitialState.FIND_GAME;
        }
        checkState("Not expecting FindGamePacket", initialState == InitialState.FIND_GAME);

        Set<GameMap> filterMaps = packet.options().maps().stream().map(ProtocolMapper::asAPI).collect(Collectors.toSet());
        int filterImpostorsCount = packet.options().impostorsCount();
        ChatLanguage filterLanguage = ProtocolMapper.asAPI(Iterables.getFirst(packet.options().languages(), null));

        // PLANNED: EVENT
        GameListPacket list = new GameListPacket();
        for (PolusGame game : getServer().getGameManager().getGames()) {
            PolusGameOptions options = game.getOptions();
            int playersCount = game.getPlayerList().getPlayers().size();
            if (playersCount == 0) {
                continue;
            }

            int maxPlayers = options.getMaxPlayers();
            GameMap map = options.getMap();
            list.totalCount().merge(ProtocolMapper.asProtocol(map), 1, Integer::sum);

            if (game.getState() == Game.State.LOBBY || playersCount >= maxPlayers) {
                continue;
            }

            int impostorsCount = options.getImpostorsCount();
            ChatLanguage language = options.getLanguage();
            if (!options.isPublic()
                    || !filterMaps.isEmpty() && !filterMaps.contains(options.getMap())
                    || filterImpostorsCount > 0 && filterImpostorsCount != impostorsCount
                    || filterLanguage != null && filterLanguage != language
            ) {
                continue;
            }

            list.games().add(new GameInfoType()
                    // .serverAddr()
                    .gameCode(game.getCodeId())
                    .hostName(game.getHostName())
                    .playersCount(playersCount)
                    .map(ProtocolMapper.asProtocol(map))
                    .impostorsCount(impostorsCount)
                    .maxPlayers(maxPlayers));
        }
        sendPacket(list);
    }

    private enum LoginState {
        NONE,
        PENDING,
        LOGGED,
    }

    private enum InitialState {
        NONE,
        CREATE_GAME,
        FIND_GAME,
        PRIVATE_GAME,
    }
}
