package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.impl.server.ethernet.ServerEthernetEndPoint;
import fr.pederobien.messenger.event.NewProtocolClientEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.EthernetProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IProtocolServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;

public class VocalServer extends ServerElement implements IEventListener {
	private final VoxyRoomImpl room;
	private final EthernetProtocolServerConfig config;
	private final IProtocolServer server;
	private final List<VocalClient> clients;
	private final Object lock;

	/**
	 * Creates a vocal server.
	 * 
	 * @param room The implementation of the room associated to this vocal server.
	 */
	protected VocalServer(VoxyRoomImpl room) {
		super(room.getServer());

		this.room = room;

		String serverName = String.format("%s-VocalServer", room.getName());
		config = Messenger.createEthernetProtocolServerConfig(VoxyProtocolManager.instance(), serverName, new ServerEthernetEndPoint(0));
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(room.getServer().getCertificate()));
		config.setConnectionName("VoxyVocalClient");
		server = Messenger.createUdpProtocolServer(config);

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
		clients.clear();

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
		return room;
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

				room.getPlayers().validate(client.getPlayer());
				info("Player %s joined %s's vocal server", client.getPlayer().getName(), room.getName());
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
		if (event.getRoom() != room.getExternal())
			return;

		config.setName(event.getRoom().getName());
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom() != room.getExternal())
			return;

		// Forcing the disconnection
		synchronized (lock) {
			Iterator<VocalClient> iterator = clients.iterator();

			while (iterator.hasNext()) {
				VocalClient client = iterator.next();
				if (client.getPlayer().getExternal().equals(event.getPlayer())) {
					Logger.info("%s - Unregistering player %s", this, event.getPlayer().getName());
					client.dispose();
					iterator.remove();
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
