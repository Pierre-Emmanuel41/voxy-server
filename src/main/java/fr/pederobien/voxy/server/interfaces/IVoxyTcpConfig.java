package fr.pederobien.voxy.server.interfaces;

import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;

public interface IVoxyTcpConfig extends IVoxyConfig {

	/**
	 * @return The properties of the server communication point.
	 */
	IServerEthernetEndPoint getPoint();
}
