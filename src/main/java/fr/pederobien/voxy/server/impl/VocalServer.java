package fr.pederobien.voxy.server.impl;

import java.util.HashMap;
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
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VocalServer implements IEventListener {
	private final VoxyRoom room;
	private final ProtocolServerConfig<IEthernetEndPoint> config;
	private final IProtocolServer server;
	private final Map<String, VocalClient> clients;
	private Object lock;

	/**
	 * Creates a UDP server associated to a room.
	 *
	 * @param room The room associated to this server.
	 */
	public VocalServer(VoxyRoom room) {
		this.room = room;

		String serverName = String.format("%s-Server", room.getName());
		config = Messenger.createServerConfig(VoxyProtocolManager.instance(), serverName, new EthernetEndPoint(0));

		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		server = Messenger.createUdpServer(config);

		clients = new HashMap<String, VocalClient>();
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
	 * @return The voxy server associated to this vocal server.
	 */
	protected IVoxyServer getServer() {
		return room.getServer();
	}

	/**
	 * @return The maps that contains the clients currently connected to this vocal server.
	 */
	protected Map<String, VocalClient> getClients() {
		return clients;
	}

	@EventHandler
	private void onClientConnected(NewProtocolClientEvent event) {
		if (event.getServer() != server)
			return;

		synchronized (lock) {
			VocalClient client = new VocalClient(this, event.getClient());

			Consumer<Boolean> callback = initialized -> {
				if (initialized) {
					clients.put(client.getPlayerName(), client);
					info("Player %s joined %s's vocal server", client.getPlayerName(), room.getName());
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
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void info(String message, Object... args) {
		Logger.info("%s - %s", server, String.format(message, args));
	}
}
