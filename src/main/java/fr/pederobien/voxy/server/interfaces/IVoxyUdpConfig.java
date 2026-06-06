package fr.pederobien.voxy.server.interfaces;

public interface IVoxyUdpConfig {

	/**
	 * @return -1 if not defined, the minimum value of the server port.
	 */
	int getMin();

	/**
	 * @return -1 if not defined, the minimum value of the server port.
	 */
	int getMax();
}
