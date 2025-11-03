package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.utils.event.Logger;

public class ServerElement {
	private final VoxyServerImpl server;

	/**
	 * Creates an element associated to a server.
	 * 
	 * @param server The server implementation associated to this element.
	 */
	protected ServerElement(VoxyServerImpl server) {
		this.server = server;
	}

	/**
	 * @return The server associated to this element.
	 */
	public VoxyServerImpl getServer() {
		return server;
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s %s", getServer(), String.format(format, args));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String format, Object... args) {
		Logger.info("%s %s", getServer(), String.format(format, args));
	}

	/**
	 * Print a log using ERROR level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void error(String format, Object... args) {
		Logger.error("%s %s", getServer(), String.format(format, args));
	}
}
