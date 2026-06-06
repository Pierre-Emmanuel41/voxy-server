package fr.pederobien.voxy.server.impl.config;

import fr.pederobien.voxy.server.interfaces.IVoxyUdpConfig;

public class VoxyUdpConfig extends VoxyConfig implements IVoxyUdpConfig {
	private int min;
	private int max;

	/**
	 * Creates a configuration for a UDP server. This configuration will be applied for room's vocal server.
	 */
	public VoxyUdpConfig() {
		min = -1;
		max = -1;
	}

	@Override
	public int getMin() {
		return min;
	}

	/**
	 * Set the minimum value of the server's port number.
	 * 
	 * @param min -1 if not defined, 0 to let the OS choosing the port number, a positive value to define the minimum.
	 */
	public void setMin(int min) {
		this.min = min;
	}

	@Override
	public int getMax() {
		return max;
	}

	/**
	 * Set the maximum value of the server's port number.
	 * 
	 * @param max -1 if not defined, 0 to let the OS choosing the port number, a positive value to define the maximum.
	 */
	public void setMax(int max) {
		this.max = max;
	}
}
