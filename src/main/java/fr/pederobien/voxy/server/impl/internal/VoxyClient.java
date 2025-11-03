package fr.pederobien.voxy.server.impl.internal;

import java.util.function.Consumer;

import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.IEventListener;

public class VoxyClient implements IEventListener {
	private final VoxyClientNotifier notifier;
	private final VoxyClientRequestHandler handler;

	/***
	 * Creates a client used to send requests to the remote.
	 *
	 * @param server The server with which this client is connected.
	 * @param client The client that gather requests that can be sent to the remote.
	 */
	public VoxyClient(VoxyServerImpl server, IProtocolClient client) {
		notifier = new VoxyClientNotifier(server, client);
		handler = new VoxyClientRequestHandler(server, client);
	}

	/**
	 * @return The player associated to this client.
	 */
	public VoxyPlayerImpl getPlayer() {
		return notifier.getPlayer();
	}

	/**
	 * Sends a request to the remote to get player's properties.
	 *
	 * @param callback The code to execute when the initialization is finished. The input parameter is the initialization status.
	 */
	public void initialize(Consumer<Boolean> callback) {
		Consumer<Boolean> postInitialization = success -> {
			if (success)
				handler.initialize(getPlayer());

			callback.accept(success);
		};

		notifier.initialize(postInitialization);
	}

	@Override
	public String toString() {
		return notifier.getClient().toString();
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		notifier.getClient().dispose();
	}
}
