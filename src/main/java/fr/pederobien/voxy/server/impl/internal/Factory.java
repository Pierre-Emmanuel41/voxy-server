package fr.pederobien.voxy.server.impl.internal;

public class Factory {

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name The server's name.
	 * @param port The port number to open.
	 */
	public static VoxyServerImpl createServerImpl(String name, int port) {
		return new VoxyServerImpl(name, port);
	}
}
