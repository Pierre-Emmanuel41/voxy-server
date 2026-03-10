package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerCloseEvent extends VoxyServerEvent {

	/**
	 * Creates an event thrown when a voxy server is closed.
	 * 
	 * @param server The server that is closed.
	 */
	public VoxyServerCloseEvent(IVoxyServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
