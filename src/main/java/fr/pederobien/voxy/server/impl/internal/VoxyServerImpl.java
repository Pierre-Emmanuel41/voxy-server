package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.server.ethernet.ServerEthernetEndPoint;
import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.messenger.event.NewProtocolClientEvent;
import fr.pederobien.messenger.event.ProtocolServerCloseEvent;
import fr.pederobien.messenger.event.ProtocolServerDisposeEvent;
import fr.pederobien.messenger.event.ProtocolServerOpenEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.EthernetProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IProtocolServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.server.event.VoxyServerCloseEvent;
import fr.pederobien.voxy.server.event.VoxyServerDisposeEvent;
import fr.pederobien.voxy.server.event.VoxyServerOpenEvent;
import fr.pederobien.voxy.server.impl.VoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerImpl implements IEventListener {
	private final String name;
	private final ICertificate certificate;
	private final EthernetProtocolServerConfig config;
	private final IProtocolServer server;
	private final RoomListImpl roomsImpl;
	private final List<VoxyClient> clients;
	private final Object lock;

	private final IVoxyServer external;

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name        The server's name.
	 * @param port        The port number to open.
	 * @param certificate The certificate to use to sign/authenticate requests.
	 */
	protected VoxyServerImpl(String name, int port, ICertificate certificate) {
		this.name = name;
		this.certificate = certificate;

		config = Messenger.createEthernetProtocolServerConfig(VoxyProtocolManager.instance(), name, new ServerEthernetEndPoint(port));
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(certificate));
		config.setConnectionName("VoxyClient");
		server = Messenger.createTcpProtocolServer(config);

		roomsImpl = new RoomListImpl(this);
		clients = new ArrayList<VoxyClient>();
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
	 * 
	 * @return True if the server is successfully opened, false otherwise.
	 */
	public boolean open() {
		AtomicBoolean success = new AtomicBoolean(server.open());
		if (success.get())
			roomsImpl.foreach(room -> success.set(success.get() && room.getVocalServer().open()));

		return success.get();
	}

	/**
	 * Close the server, each player currently connected will be kicked.
	 * 
	 * @return True if the server is successfully closed, false otherwise.
	 */
	public boolean close() {
		AtomicBoolean success = new AtomicBoolean(server.close());

		if (success.get()) {

			// Notifying each client that the server is closing
			for (VoxyClient client : clients)
				client.onServerClosed();

			// Clearing the list of clients
			clients.clear();

			roomsImpl.foreach(room -> success.set(success.get() && room.onServerClosed()));
		}

		return success.get();
	}

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 * 
	 * @return True if the server is successfully disposed, false otherwise.
	 */
	public boolean dispose() {
		AtomicBoolean success = new AtomicBoolean(server.dispose());
		if (success.get())
			roomsImpl.foreach(room -> success.set(success.get() && room.getVocalServer().dispose()));

		return success.get();
	}

	/**
	 * @return True if the server is opened, false otherwise.
	 */
	public boolean isOpened() {
		return server.isOpened();
	}

	/**
	 * @return True if this server is disposed, false otherwise.
	 */
	public boolean isDisposed() {
		return server.isDisposed();
	}

	/**
	 * @return The list of rooms associated to this server.
	 */
	public RoomListImpl getRooms() {
		return roomsImpl;
	}

	/**
	 * @return A copy of the underlying list of players connected to the server.
	 */
	public List<IVoxyPlayer> getPlayers() {
		List<IVoxyPlayer> list = new ArrayList<IVoxyPlayer>();
		synchronized (lock) {
			for (VoxyClient client : clients)
				list.add(client.getPlayer().getExternal());
		}

		return list;
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
			for (VoxyClient client : clients)
				if (client.getPlayer().getName().equals(name))
					return client.getPlayer();
		}

		return null;
	}

	/**
	 * Removes the player associated to the given name.
	 * 
	 * @param player The player to remove.
	 */
	public void remove(VoxyPlayerImpl player) {
		synchronized (lock) {
			for (int i = 0; i < clients.size(); i++)
				if (clients.get(i).getPlayer() == player) {
					clients.remove(i);
					break;
				}
		}
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
	 * @return The certificate to use to sign/authenticate requests.
	 */
	public ICertificate getCertificate() {
		return certificate;
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
					clients.add(client);
				} else {
					info("Failure to initialize connection with client %s, disposing connection", client);
					client.dispose();
				}
			};

			// Adding delay to let the client be ready to handle server's initialization sequence
			try {
				int delay = 500;

				debug("Requiring player's properties in %s ms", delay);
				Thread.sleep(delay);

				client.initialize(callback);
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	@EventHandler
	private void onServerOpen(ProtocolServerOpenEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerOpenEvent(external));
	}

	@EventHandler
	private void onServerClose(ProtocolServerCloseEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerCloseEvent(external));
	}

	@EventHandler
	private void onServerDispose(ProtocolServerDisposeEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerDisposeEvent(external));
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s - %s", this, String.format(format, args));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String format, Object... args) {
		Logger.info("%s - %s", this, String.format(format, args));
	}
}
