package fr.pederobien.voxy.server.impl;

import fr.pederobien.voxy.server.impl.config.VoxyServerConfig;
import fr.pederobien.voxy.server.impl.internal.Factory;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyServerConfig;

public class VoxyServerFactory {

	/**
	 * Creates a configuration for a voxy server.
	 * 
	 * @param name    The name of the server.
	 * @param address The IP address of the server.
	 * @param port    The port number of the server.
	 * @return The created configuration.
	 */
	public static final VoxyServerConfig createConfig(String name, String address, int port) {
		return new VoxyServerConfig(name, address, port);
	}

	/**
	 * Creates a voxy server.
	 * 
	 * @param config The configuration that gather server's parameters.
	 * @return The created voxy server.
	 */
	public static IVoxyServer createServer(IVoxyServerConfig config) {
		return Factory.createServerImpl(config).getExternal();
	}

	/**
	 * Creates a voxy server with default configuration parameters.
	 * 
	 * @param name    The server's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 * 
	 * @return The created voxy server.
	 */
	public static IVoxyServer createDefault(String name, String address, int port) {
		return createServer(createConfig(name, address, port));
	}

	/**
	 * Creates a voxy server with default configuration parameters.
	 * 
	 * @param name    The server's name.
	 * @param address The server's address.
	 * @param port    The server's port number.
	 * 
	 * @return The created voxy server.
	 */
	public static IVoxyServer createDefault(String name, int port) {
		return createDefault(name, "*", port);
	}
}
