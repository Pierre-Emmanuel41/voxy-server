package fr.pederobien.voxy.server.impl;

import fr.pederobien.communication.impl.EthernetEndPoint;
import fr.pederobien.communication.impl.layer.AesSafeLayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.ProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IProtocolServer;
import fr.pederobien.voxy.common.impl.VoxyProtocolManager;

public class VocalServer {
	private final ProtocolServerConfig<IEthernetEndPoint> config;
	private final IProtocolServer server;

	/**
	 * Creates a UDP server associated to a room.
	 *
	 * @param room The room associated to this server.
	 */
	public VocalServer(VoxyRoom room) {
		String serverName = String.format("%s-Server", room.getName());
		config = Messenger.createServerConfig(VoxyProtocolManager.instance(), serverName, new EthernetEndPoint(0));

		// TODO: Replace SimpleCertificate by a proper one
		config.setLayerInitializer(() -> new AesSafeLayerInitializer(new SimpleCertificate()));

		server = Messenger.createUdpServer(config);
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
}
