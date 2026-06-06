package fr.pederobien.voxy.server.interfaces;

public interface IVoxyServerConfig {

	/**
	 * @return The name of the server.
	 */
	String getName();

	/**
	 * @return The configuration to use for the main TCP server.
	 */
	IVoxyTcpConfig getTcpConfig();

	/**
	 * @return The configuration to use for the room's vocal server.
	 */
	IVoxyUdpConfig getUdpConfig();
}
