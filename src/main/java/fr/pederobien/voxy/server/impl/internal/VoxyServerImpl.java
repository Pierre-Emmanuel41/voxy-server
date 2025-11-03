package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
import fr.pederobien.voxy.server.impl.VoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerImpl implements IEventListener {
	private final String name;
	private final ProtocolServerConfig<IEthernetEndPoint> config;
	private final IProtocolServer server;
	private final RoomListImpl roomsImpl;
	private final List<VoxyPlayerImpl> players;
	private final Object lock;

	private final IVoxyServer external;

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name The server's name.
	 * @param port The port number to open.
	 */
	protected VoxyServerImpl(String name, int port) {
		this.name = name;
		config = Messenger.createServerConfig(VoxyProtocolManager.instance(), name, new EthernetEndPoint(port));

		// TODO: Replace SimpleCertificate by a proper one
		// config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		server = Messenger.createTcpServer(config);

		roomsImpl = new RoomListImpl(this);
		players = new ArrayList<VoxyPlayerImpl>();
		lock = new Object();

		external = new VoxyServer(this);

		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return server.toString();
	}

	/**
	 * @return The voxy server name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Open the server to let the clients connect.
	 */
	public void open() {
		server.open();
	}

	/**
	 * Close the server, each player currently connected will be kicked.
	 */
	public void close() {
		server.close();
	}

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 */
	public void dispose() {
		server.dispose();
	}

	/**
	 * @return The list of rooms associated to this server.
	 */
	public RoomListImpl getRooms() {
		return roomsImpl;
	}

	/**
	 * Get the player implementation associated to the given name.
	 * 
	 * @param name The player's name.
	 * 
	 * @return The associated implementation if registered, null otherwise.
	 */
	public VoxyPlayerImpl getPlayerByName(String name) {
		synchronized (lock) {
			for (VoxyPlayerImpl playerImpl : players)
				if (playerImpl.getName().equals(name))
					return playerImpl;
		}

		return null;
	}

	/**
	 * Check if a player with the given name is registered on the server.
	 * 
	 * @param name The player's name to check.
	 * 
	 * @return True if the player is registered, false otherwise.
	 */
	public boolean isRegistered(String name) {
		return getPlayerByName(name) != null;
	}

	/**
	 * @return The voxy server to use externally.
	 */
	public IVoxyServer getExternal() {
		return external;
	}

	@EventHandler
	private void onNewClientConnected(NewProtocolClientEvent event) {
		if (event.getServer() != server)
			return;

		synchronized (lock) {
			VoxyClient client = new VoxyClient(this, event.getClient());
			Consumer<Boolean> callback = initialized -> {
				if (initialized) {
					info("Player %s joined the server", client.getPlayer().getName());
					players.add(client.getPlayer());
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
