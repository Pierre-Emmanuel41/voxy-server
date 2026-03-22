package fr.pederobien.voxy.server.impl;

import fr.pederobien.communication.interfaces.layer.ICertificate;
import fr.pederobien.communication.testing.tools.SimpleCertificate;
import fr.pederobien.voxy.server.impl.internal.Factory;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServerFactory {

	/**
	 * Creates a voxy server.
	 * 
	 * @param name        The server name.
	 * @param port        The server port number.
	 * @param certificate The certificate to use to sign/authenticate requests.
	 * 
	 * @return The created voxy server.
	 */
	public static IVoxyServer create(String name, int port, ICertificate certificate) {
		return Factory.createServerImpl(name, port, certificate).getExternal();
	}

	/**
	 * Creates a voxy server. The certificate used to sign/authenticate requests is weak, it is recommended to use this method for
	 * development only.
	 * 
	 * @param name The server name.
	 * @param port The server port number.
	 * 
	 * @return The created voxy server.
	 */
	public static IVoxyServer create(String name, int port) {
		return Factory.createServerImpl(name, port, new SimpleCertificate()).getExternal();
	}
}
