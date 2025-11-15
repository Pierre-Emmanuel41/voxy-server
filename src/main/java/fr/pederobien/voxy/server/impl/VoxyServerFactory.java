package fr.pederobien.voxy.server.impl;

import fr.pederobien.voxy.server.impl.internal.Factory;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerFactory {

	/**
	 * Creates a voxy server.
	 * 
	 * @param name The server name.
	 * @param port The server port number.
	 * 
	 * @return The created voxy server.
	 */
	public static IVoxyServer create(String name, int port) {
		return Factory.createServerImpl(name, port).getExternal();
	}
}
