package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.IVoxyServerConfig;

public class Factory {

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name        The server's name.
	 * @param port        The port number to open.
	 * @param certificate The certificate to use to sign/authenticate requests.
	 */
	public static VoxyServerImpl createServerImpl(IVoxyServerConfig config) {
		return new VoxyServerImpl(config);
	}
}
