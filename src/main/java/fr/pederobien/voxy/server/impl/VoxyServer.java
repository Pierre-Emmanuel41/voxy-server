package fr.pederobien.voxy.server.impl;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.messenger.event.NewProtocolClientEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.ProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IProtocolServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.server.event.*;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

import java.util.*;
import java.util.function.Consumer;

public class VoxyServer implements IVoxyServer, IEventListener {
    private final ProtocolServerConfig<IEthernetEndPoint> config;
    private final IProtocolServer server;
    private final List<VoxyClient> clients;
    private final Map<String, IVoxyPlayer> players;
    private final Map<String, IVoxyRoom> rooms;
    private final Object lock;

    /**
     * Creates a vocal server.
     *
     * @param name The server's name.
     * @param port The port number to open.
     */
    public VoxyServer(String name, int port) {
        config = Messenger.createServerConfig(VoxyProtocolManager.instance(), name, new EthernetEndPoint(port));

        // TODO: Replace SimpleCertificate by a proper one
        // config.setLayerInitializer(() -> new AesLayerInitializer(new SimpleCertificate()));

        server = Messenger.createTcpServer(config);

        clients = new ArrayList<VoxyClient>();
        players = new HashMap<String, IVoxyPlayer>();
        rooms = new HashMap<String, IVoxyRoom>();

        // Lock to prevent simultaneous list modifications
        lock = new Object();
        EventManager.registerListener(this);
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void open() {
        server.open();
    }

    @Override
    public void close() {
        server.close();
    }

    @Override
    public void dispose() {
        server.dispose();
    }

    @Override
    public Map<String, IVoxyPlayer> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    @Override
    public Map<String, IVoxyRoom> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    @Override
    public void add(String name) {
        synchronized (lock) {
            boolean registered = rooms.get(name) != null;

            // Room's name shall be unique
            if (!registered) {
                // Notifying first that a room is about to be added, if event not cancelled then the room is added
                AddRoomPreEvent preEvent = new AddRoomPreEvent(this, name);
                Runnable exe = () -> {
                    IVoxyRoom room = new VoxyRoom(this, name);
                    info("Room %s has been added", room.getName());
                    rooms.put(name, room);
                    EventManager.callEvent(new AddRoomPostEvent(this, room));
                };

                EventManager.callEvent(preEvent, exe);
            }
        }
    }

    @Override
    public void remove(String name) {
        synchronized (lock) {
            Iterator<Map.Entry<String, IVoxyRoom>> iterator = rooms.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, IVoxyRoom> entry = iterator.next();
                if (entry.getKey().equals(name)) {

                    // Notifying first that a room is about to be removed, if event not cancelled then the room is removed
                    RemoveRoomPrevent preEvent = new RemoveRoomPrevent(this, entry.getValue());
                    Runnable exe = () -> {
                        info("Room %s has been removed", entry.getValue().getName());
                        iterator.remove();
                        EventManager.callEvent(new RemoveRoomPostEvent(this, entry.getValue()));
                    };

                    EventManager.callEvent(preEvent, exe);

                    // Room's name is unique
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return server.toString();
    }

    @EventHandler
    private void onNewClientConnected(NewProtocolClientEvent event) {
        if (event.getServer() != server)
            return;

        synchronized (lock) {
            VoxyClient client = new VoxyClient(this, event.getClient());
            Consumer<Boolean> callback = initialized -> {
                if (initialized) {
                    clients.add(client);
                    info("Player %s joined the server", client.getPlayer().getName());
                    players.put(client.getPlayer().getName(), client.getPlayer());
                } else
                    client.dispose();
            };

            client.initialize(callback);
        }
    }

    @EventHandler
    private void onRoomRenamed(RenameRoomPostEvent event) {
        if (event.getRoom().getServer() != this)
            return;

        synchronized (lock) {
            rooms.remove(event.getOldName());
            rooms.put(event.getRoom().getName(), event.getRoom());
        }
    }

    /**
     * Creates a LogEvent with log level INFO and the given formatted text.
     *
     * @param format The formatter if the message to display has arguments.
     * @param args   The arguments of the message to display.
     */
    private void info(String format, Object... args) {
        Logger.info("%s %s", this, String.format(format, args));
    }
}
