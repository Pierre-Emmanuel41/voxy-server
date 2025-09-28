package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerEvent extends VoxyEvent {
	private final IVoxyServer server;

	/**
	 * Creates a voxy server event.
	 *
	 * @param server The server involved in this event.
	 */
	public VoxyServerEvent(IVoxyServer server) {
		this.server = server;
	}

	/**
	 * @return The server involved in this event.
	 */
	public IVoxyServer getServer() {
		return server;
	}
}
