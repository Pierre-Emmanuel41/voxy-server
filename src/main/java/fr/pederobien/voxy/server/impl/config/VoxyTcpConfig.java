package fr.pederobien.voxy.server.impl.config;

import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.voxy.server.interfaces.IVoxyTcpConfig;

public class VoxyTcpConfig extends VoxyConfig implements IVoxyTcpConfig {
	private final IServerEthernetEndPoint endPoint;

	/**
	 * Creates a configuration for the TCP server. The TCP server is the main server with which the clients will communicate.
	 * 
	 * @param endPoint The server's end-point (IP + port number).
	 */
	public VoxyTcpConfig(IServerEthernetEndPoint endPoint) {
		this.endPoint = endPoint;
	}

	@Override
	public IServerEthernetEndPoint getPoint() {
		return endPoint;
	}

}
