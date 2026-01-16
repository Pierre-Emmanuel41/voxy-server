package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;

public class VocalServer extends ServerElement implements IEventListener {
	private final VoxyRoomImpl roomImpl;
	private final ProtocolServerConfig<IEthernetEndPoint> config;
	private final IProtocolServer server;
	private final List<VocalClient> clients;
	private final Object lock;

	/**
	 * Creates a vocal server.
	 * 
	 * @param roomImpl The implementation of the room associated to this vocal server.
	 */
	protected VocalServer(VoxyRoomImpl roomImpl) {
		super(roomImpl.getServer());

		this.roomImpl = roomImpl;

		String serverName = String.format("%s-VocalServer", roomImpl.getName());
		config = Messenger.createServerConfig(VoxyProtocolManager.instance(), serverName, new EthernetEndPoint(0));

		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		// The name to use when a client logs
		config.setConnectionName("VoxyVocalClient");

		server = Messenger.createUdpServer(config);

		clients = new ArrayList<VocalClient>();
		lock = new Object();

		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return server.toString();
	}

	/**
	 * Starts the server and wait for a client to be connected.
	 *
	 * @return True if the server is in correct state to be opened, false otherwise.
	 */
	public boolean open() {
		return server.open();
	}

	/**
	 * Stops the server, dispose the connection with each client.
	 *
	 * @return True if the server is in correct state to be closed, false otherwise.
	 */
	public boolean close() {
		return server.close();
	}

	/**
	 * Dispose this server. It cannot be used anymore.
	 *
	 * @return True if the server has been disposed, false otherwise.
	 */
	public boolean dispose() {
		return server.dispose();
	}

	/**
	 * @return The UDP port number with which this server is bound.
	 */
	public int getPort() {
		return config.getPoint().getPort();
	}

	/**
	 * @return The room implementation associated to this vocal server.
	 */
	public VoxyRoomImpl getRoom() {
		return roomImpl;
	}

	@EventHandler
	private void onClientConnected(NewProtocolClientEvent event) {
		if (event.getServer() != server)
			return;

		VocalClient client = new VocalClient(this, event.getClient());

		Consumer<Boolean> callback = initialized -> {
			if (initialized) {

				// Registering the client
				synchronized (lock) {
					clients.add(client);
				}

				roomImpl.getPlayers().validate(client.getPlayer());
				info("Player %s joined %s's vocal server", client.getPlayer().getName(), roomImpl.getName());
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

	@EventHandler
	private void onRoomRenamed(RenameRoomPostEvent event) {
		if (event.getRoom() != roomImpl.getExternal())
			return;

		config.setName(event.getRoom().getName());
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom() != roomImpl.getExternal())
			return;

		// Forcing the disconnection
		synchronized (lock) {
			Iterator<VocalClient> iterator = clients.iterator();

			while (iterator.hasNext()) {
				VocalClient client = iterator.next();
				if (client.getPlayer().getExternal().equals(event.getPlayer())) {
					client.dispose();
					iterator.remove();
					Logger.info("%s - Unregistering player %s", this, event.getPlayer().getName());
					break;
				}
			}
		}
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
