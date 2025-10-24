package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.event.NewProtocolClientEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.ProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IProtocolServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServer implements IVoxyServer, IEventListener {
	private final ProtocolServerConfig<IEthernetEndPoint> config;
	private final IProtocolServer server;
	private final List<VoxyClient> clients;
	private final IRoomList rooms;
	private final Map<String, IVoxyPlayer> players;
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
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		server = Messenger.createTcpServer(config);

		clients = new ArrayList<VoxyClient>();
		rooms = new RoomList(this);
		players = new HashMap<String, IVoxyPlayer>();

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
	public IRoomList getRooms() {
		return rooms;
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
				} else {
					info("Failure to initialize connection with client %s, disposing connection", client);
					client.dispose();
				}
			};

			// Adding delay to let the client be ready to handle server's initialization sequence
			try {
				Thread.sleep(500);
				client.initialize(callback);
			} catch (Exception e) {
				// Do nothing
			}
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
