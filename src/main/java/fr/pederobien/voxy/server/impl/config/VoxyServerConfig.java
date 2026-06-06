package fr.pederobien.voxy.server.impl.config;

import fr.pederobien.communication.impl.server.ethernet.ServerEthernetEndPoint;
import fr.pederobien.voxy.server.interfaces.IVoxyServerConfig;

public class VoxyServerConfig implements IVoxyServerConfig {
	private final String name;
	private final VoxyTcpConfig tcpConfig;
	private final VoxyUdpConfig udpConfig;

	/**
	 * Creates a configuration for a voxy server.
	 * 
	 * @param name    The name of the server.
	 * @param address The IP address of the TCP server.
	 * @param port    The port number of the TCP server.
	 */
	public VoxyServerConfig(String name, String address, int port) {
		this.name = name;
		tcpConfig = new VoxyTcpConfig(new ServerEthernetEndPoint(address, port));
		udpConfig = new VoxyUdpConfig();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public VoxyTcpConfig getTcpConfig() {
		return tcpConfig;
	}

	@Override
	public VoxyUdpConfig getUdpConfig() {
		return udpConfig;
	}

}
