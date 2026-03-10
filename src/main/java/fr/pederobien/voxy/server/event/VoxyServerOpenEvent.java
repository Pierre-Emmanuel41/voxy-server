package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerOpenEvent extends VoxyServerEvent {

	/**
	 * Creates an event thrown when a voxy server is opened.
	 * 
	 * @param server The server that is opened.
	 */
	public VoxyServerOpenEvent(IVoxyServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
