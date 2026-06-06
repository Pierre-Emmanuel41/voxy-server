package fr.pederobien.voxy.server.impl.config;

import java.util.function.Supplier;

import fr.pederobien.communication.impl.layer.LayerInitializer;
import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;
import fr.pederobien.communication.interfaces.server.IClientValidator;
import fr.pederobien.voxy.server.interfaces.IVoxyConfig;

public class VoxyConfig implements IVoxyConfig {
	private int connectionMaxUnstableCounter;
	private int connectionHealTime;
	private Supplier<ILayerInitializer> layerInitializer;
	private IClientValidator<IEthernetEndPoint> clientValidator;
	private int serverMaxUnstableCounter;
	private int serverHealTime;

	/**
	 * Creates a simple voxy config that gather parameters for TCP and UDP servers.
	 */
	protected VoxyConfig() {
		connectionMaxUnstableCounter = 10;
		connectionHealTime = 1000;
		layerInitializer = () -> new LayerInitializer();
		clientValidator = _ -> true;
		serverMaxUnstableCounter = 5;
		serverHealTime = 1000;
	}

	@Override
	public int getConnectionMaxUnstableCounter() {
		return connectionMaxUnstableCounter;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. The
	 * connection max counter value is the maximum value the unstable counter can reach before throwing a connection unstable event.
	 *
	 * @param connectionMaxUnstableCounter The maximum value the connection's unstable counter can reach.
	 */
	public void setConnectionMaxUnstableCounter(int connectionMaxUnstableCounter) {
		this.connectionMaxUnstableCounter = connectionMaxUnstableCounter;
	}

	@Override
	public int getConnectionHealTime() {
		return connectionHealTime;
	}

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. During the
	 * connection lifetime, it is likely possible that the connection become unstable. However, if the connection is stable the
	 * counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after which the
	 * connection's error counter is decremented.
	 *
	 * @param connectionHealTime The time, in ms, after which the connection's error counter is decremented.
	 */
	public void setConnectionHealTime(int connectionHealTime) {
		this.connectionHealTime = connectionHealTime;
	}

	@Override
	public Supplier<ILayerInitializer> getLayerInitializer() {
		return layerInitializer;
	}

	/**
	 * Set how a layer must be initialized.
	 *
	 * @param layerInitializer The initialisation sequence.
	 */
	public void setLayerInitializer(Supplier<ILayerInitializer> layerInitializer) {
		this.layerInitializer = layerInitializer;
	}

	@Override
	public IClientValidator<IEthernetEndPoint> getClientValidator() {
		return clientValidator;
	}

	/**
	 * Set the server client validator.
	 *
	 * @param clientValidator The validator to authorize a client to be connected to the server.
	 */
	public void setClientValidator(IClientValidator<IEthernetEndPoint> clientValidator) {
		this.clientValidator = clientValidator;
	}

	@Override
	public int getServerMaxUnstableCounter() {
		return serverMaxUnstableCounter;
	}

	/**
	 * The server is monitored when waiting for a new client, validating client end-point and initialising the connection with the
	 * remote. During the server lifetime, it is likely possible that the server become unstable. The server's max counter is the
	 * maximum value the unstable counter can reach before throwing a server unstable event and closing the server. This counter is
	 * incremented each time an exception is happening.
	 *
	 * @param serverMaxUnstableCounter The maximum value the server's unstable counter can reach.
	 */
	public void setServerMaxUnstableCounter(int serverMaxUnstableCounter) {
		this.serverMaxUnstableCounter = serverMaxUnstableCounter;
	}

	@Override
	public int getServerHealTime() {
		return serverHealTime;
	}

	/**
	 * The server is monitored when waiting for a new client, validating client end-point and initialising the connection with the
	 * remote. During the server lifetime, it is likely possible that the server become unstable. However, if the server is stable the
	 * unstable counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after
	 * which the server's error counter is decremented.
	 *
	 * @param serverHealTime The time, in ms, after which the server's error counter is decremented.
	 */
	public void setServerHealTime(int serverHealTime) {
		this.serverHealTime = serverHealTime;
	}
}
