package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.communication.interfaces.layer.ICertificate;

public class Factory {

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name        The server's name.
	 * @param port        The port number to open.
	 * @param certificate The certificate to use to sign/authenticate requests.
	 */
	public static VoxyServerImpl createServerImpl(String name, int port, ICertificate certificate) {
		return new VoxyServerImpl(name, port, certificate);
	}
}
