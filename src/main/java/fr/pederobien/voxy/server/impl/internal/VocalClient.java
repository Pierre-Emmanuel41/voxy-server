package fr.pederobien.voxy.server.impl.internal;

import java.util.function.Consumer;

import fr.pederobien.messenger.event.ProtocolConnectionLostEvent;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;

public class VocalClient extends ServerElement implements IEventListener {
	private final VocalServer server;
	private final IProtocolClient client;
	private String playerName;

	/**
	 * Creates a vocal client.
	 * 
	 * @param server The vocal server associated to this vocal client.
	 * @param client The client used to communicate with the remote.
	 */
	protected VocalClient(VocalServer server, IProtocolClient client) {
		super(server.getServer());

		this.server = server;
		this.client = client;
	}

	@Override
	public String toString() {
		return client.toString();
	}

	/**
	 * Sends a request to the remote to get player's properties.
	 *
	 * @param callback The code to execute when the initialization is finished. The input parameter is the initialization status.
	 */
	public void initialize(Consumer<Boolean> callback) {
		// Sending request to get player's properties
		debug("%s - Requiring player's properties", server);
		IRequestMessage request = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, new PlayerPropertiesRequest());
		request.setCallback(args -> {
			boolean success = false;
			if (!args.isTimeout() && handlePlayerProperties(args.identifier(), args.response())) {
				success = true;
				EventManager.registerListener(this);
			}

			callback.accept(success);
		});

		client.getConnection().send(request);
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		client.dispose();
	}

	/**
	 * @return The name of the player.
	 */
	public String getPlayerName() {
		return playerName;
	}

	@EventHandler
	private void onConnectionLost(ProtocolConnectionLostEvent event) {
		if (event.getConnection() != client.getConnection())
			return;

		EventManager.unregisterListener(this);
		server.remove(this);
	}

	/**
	 * Parses the raw bytes array, check if the player already exists, and sends back to the client an error code if he cannot join
	 * the server.
	 *
	 * @param messageID The identifier of the client's response.
	 * @param data      The raw bytes array containing the client's response.
	 * 
	 * @returns True if the player can join the server, false otherwise.
	 */
	private boolean handlePlayerProperties(int messageID, byte[] data) {
		PlayerPropertiesRequest payload = (PlayerPropertiesRequest) client.parse(data).getPayload();

		if (payload == null) {
			debug("%s - Technical error happened: Could not parse client's response for player's properties", server);
			return false;
		}

		if (!getServer().isRegistered(payload.getName())) {
			debug("%s - Denying %s, there is no player with this name registered on the voxy server", server, payload.getName());
			IRequestMessage response = getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_DOES_NOT_EXIST, null);
			response.setSync(true);
			client.getConnection().answer(messageID, response);
			return false;
		}

		if (!server.isRegistered(payload.getName())) {
			debug("%s - Denying %s, no player is registered", server, payload.getName());
			IRequestMessage response = getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NOT_REGISTERED, null);
			response.setSync(true);
			client.getConnection().answer(messageID, response);
			return false;
		}

		playerName = payload.getName();

		debug("%s - Accepting player %s", server, playerName);
		client.getConnection().answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		return true;
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param error      The request error.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
		return client.getRequest(identifier, error, payload);
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}
}
