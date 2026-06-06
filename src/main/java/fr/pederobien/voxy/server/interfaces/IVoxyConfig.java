package fr.pederobien.voxy.server.interfaces;

import java.util.function.Supplier;

import fr.pederobien.communication.interfaces.IEthernetEndPoint;
import fr.pederobien.communication.interfaces.layer.ILayerInitializer;
import fr.pederobien.communication.interfaces.server.IClientValidator;

public interface IVoxyConfig {

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. The
	 * connection max counter value is the maximum value the unstable counter can reach before throwing a connection unstable event.
	 *
	 * @return The maximum value the connection's unstable counter can reach.
	 */
	int getConnectionMaxUnstableCounter();

	/**
	 * The connection to the remote is monitored so that if an error is happening, a counter is incremented automatically. During the
	 * connection lifetime, it is likely possible that the connection become unstable. However, if the connection is stable the
	 * counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after which the
	 * connection's error counter is decremented.
	 *
	 * @return The time, in ms, after which the connection's error counter is decremented.
	 */
	int getConnectionHealTime();

	/**
	 * @return An object that specify how a layer must be initialized.
	 */
	Supplier<ILayerInitializer> getLayerInitializer();

	/**
	 * @return The validator to authorize or not the client to be connected to the server.
	 */
	IClientValidator<IEthernetEndPoint> getClientValidator();

	/**
	 * The server is monitored when waiting for a new client, validating client end-point and initialising the connection with the
	 * remote. During the server lifetime, it is likely possible that the server become unstable. The server's max counter is the
	 * maximum value the unstable counter can reach before throwing a server unstable event and closing the server. This counter is
	 * incremented each time an exception is happening.
	 *
	 * @return The maximum value the server's unstable counter can reach.
	 */
	int getServerMaxUnstableCounter();

	/**
	 * The server is monitored when waiting for a new client, validating client end-point and initialising the connection with the
	 * remote. During the server lifetime, it is likely possible that the server become unstable. However, if the server is stable the
	 * unstable counter value should be 0 as no error happened for a long time. The heal time, in milliseconds, is the time after
	 * which the server's error counter is decremented.
	 *
	 * @return The time, in ms, after which the server's error counter is decremented.
	 */
	int getServerHealTime();
}
