package kinoko.server.node;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import kinoko.packet.CentralPacket;
import kinoko.server.ServerConstants;
import kinoko.server.messenger.Messenger;
import kinoko.server.messenger.MessengerStorage;
import kinoko.server.messenger.MessengerUser;
import kinoko.server.migration.MigrationInfo;
import kinoko.server.migration.MigrationStorage;
import kinoko.server.netty.CentralPacketDecoder;
import kinoko.server.netty.CentralPacketEncoder;
import kinoko.server.netty.CentralServerHandler;
import kinoko.server.netty.NettyContext;
import kinoko.server.party.Party;
import kinoko.server.party.PartyStorage;
import kinoko.server.user.RemoteUser;
import kinoko.server.user.UserStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class CentralServerNode extends Node {
    private static final Logger log = LogManager.getLogger(CentralServerNode.class);
    private final ServerStorage serverStorage = new ServerStorage();
    private final MigrationStorage migrationStorage = new MigrationStorage();
    private final UserStorage userStorage = new UserStorage();
    private final PartyStorage partyStorage = new PartyStorage();
    private final MessengerStorage messengerStorage = new MessengerStorage();

    private final CompletableFuture<?> initializeFuture = new CompletableFuture<>();
    private final CompletableFuture<?> shutdownFuture = new CompletableFuture<>();
    private ChannelFuture centralServerFuture;


    // CHANNEL METHODS -------------------------------------------------------------------------------------------------

    public synchronized void addServerNode(RemoteServerNode serverNode) {
        serverStorage.addServerNode(serverNode);
        if (serverStorage.isFull()) {
            initializeFuture.complete(null);
        }
    }

    public synchronized void removeServerNode(int channelId) {
        serverStorage.removeServerNode(channelId);
        if (serverStorage.isEmpty()) {
            shutdownFuture.complete(null);
        }
    }

    public Optional<RemoteServerNode> getChannelServerNodeById(int channelId) {
        return serverStorage.getChannelServerNodeById(channelId);
    }

    public List<RemoteServerNode> getChannelServerNodes() {
        return serverStorage.getChannelServerNodes();
    }


    // MIGRATION METHODS -----------------------------------------------------------------------------------------------

    public boolean isOnline(int accountId) {
        return migrationStorage.isMigrating(accountId) || userStorage.getByAccountId(accountId).isPresent();
    }

    public boolean isMigrating(int accountId) {
        return migrationStorage.isMigrating(accountId);
    }

    public boolean submitMigrationRequest(MigrationInfo migrationInfo) {
        return migrationStorage.submitMigrationRequest(migrationInfo);
    }

    public Optional<MigrationInfo> completeMigrationRequest(int channelId, int accountId, int characterId, byte[] machineId, byte[] clientKey) {
        return migrationStorage.completeMigrationRequest(channelId, accountId, characterId, machineId, clientKey);
    }


    // USER METHODS ----------------------------------------------------------------------------------------------------

    public Optional<RemoteUser> getUserByCharacterId(int characterId) {
        return userStorage.getByCharacterId(characterId);
    }

    public Optional<RemoteUser> getUserByCharacterName(String characterName) {
        return userStorage.getByCharacterName(characterName);
    }

    public void addUser(RemoteUser remoteUser) {
        userStorage.putUser(remoteUser);
        getChannelServerNodeById(remoteUser.getChannelId()).ifPresent(RemoteServerNode::incrementUserCount);
    }

    public void updateUser(RemoteUser remoteUser) {
        userStorage.putUser(remoteUser);
    }

    public void removeUser(RemoteUser remoteUser) {
        userStorage.removeUser(remoteUser);
        getChannelServerNodeById(remoteUser.getChannelId()).ifPresent(RemoteServerNode::decrementUserCount);
    }


    // PARTY METHODS ---------------------------------------------------------------------------------------------------

    public Party createNewParty(RemoteUser remoteUser) {
        final Party party = new Party(partyStorage.getNewPartyId(), remoteUser);
        partyStorage.addParty(party);
        return party;
    }

    public boolean removeParty(Party party) {
        return partyStorage.removeParty(party);
    }

    public Optional<Party> getPartyById(int partyId) {
        if (partyId == 0) {
            return Optional.empty();
        }
        return partyStorage.getPartyById(partyId);
    }

    public Optional<Party> getPartyByCharacterId(int characterId) {
        return partyStorage.getPartyByCharacterId(characterId);
    }


    // MESSENGER METHODS -----------------------------------------------------------------------------------------------

    public Messenger createNewMessenger(RemoteUser remoteUser, MessengerUser messengerUser) {
        final Messenger messenger = new Messenger(messengerStorage.getNewMessengerId());
        messenger.addUser(remoteUser, messengerUser);
        messengerStorage.addMessenger(messenger);
        return messenger;
    }

    public boolean removeMessenger(Messenger messenger) {
        return messengerStorage.removeMessenger(messenger);
    }

    public Optional<Messenger> getMessengerById(int messengerId) {
        if (messengerId == 0) {
            return Optional.empty();
        }
        return messengerStorage.getMessengerById(messengerId);
    }


    // OVERRIDES -------------------------------------------------------------------------------------------------------

    @Override
    public void initialize() throws InterruptedException {
        // Start central server
        final CentralServerNode self = this;
        centralServerFuture = startServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new CentralPacketDecoder(), new CentralServerHandler(self), new CentralPacketEncoder());
                ch.attr(NettyContext.CONTEXT_KEY).set(new NettyContext());
                ch.attr(RemoteServerNode.NODE_KEY).set(new RemoteServerNode(ch));
                ch.writeAndFlush(CentralPacket.initializeRequest());
            }
        }, ServerConstants.CENTRAL_PORT);
        centralServerFuture.sync();
        log.info("Central server listening on port {}", ServerConstants.CENTRAL_PORT);

        // Wait for child node connections
        final Instant start = Instant.now();
        initializeFuture.join();
        log.info("All servers connected in {} milliseconds", Duration.between(start, Instant.now()).toMillis());

        // Complete initialization for login server node
        final RemoteServerNode loginServerNode = serverStorage.getLoginServerNode().orElseThrow();
        loginServerNode.write(CentralPacket.initializeComplete(serverStorage.getChannelServerNodes()));
    }

    @Override
    public void shutdown() throws InterruptedException {
        // Shutdown login server node
        final Instant start = Instant.now();
        serverStorage.getLoginServerNode().ifPresent((serverNode) -> serverNode.write(CentralPacket.shutdownRequest()));

        // Shutdown channel server nodes
        for (RemoteServerNode serverNode : serverStorage.getChannelServerNodes()) {
            serverNode.write(CentralPacket.shutdownRequest());
        }
        shutdownFuture.join();
        log.info("All servers disconnected in {} milliseconds", Duration.between(start, Instant.now()).toMillis());

        // Close central server
        centralServerFuture.channel().close().sync();
        log.info("Central server closed");
    }
}
