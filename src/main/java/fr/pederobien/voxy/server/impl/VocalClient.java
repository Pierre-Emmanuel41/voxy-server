package fr.pederobien.voxy.server.impl;

import java.util.function.Consumer;

import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;

public class VocalClient extends ClientWrapper implements IEventListener {
	private final VocalServer server;
	private String playerName;

	/**
	 * Creates a client to handle vocal communication.
	 * 
	 * @param server The room associated to this vocal client.
	 * @param client The protocol client associated to this vocal client.
	 */
	public VocalClient(VocalServer server, IProtocolClient client) {
		super(client);
		this.server = server;
	}

	@Override
	public String toString() {
		return getClient().toString();
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

		send(request);
	}

	/**
	 * @return The name of the player.
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		getClient().dispose();
	}

	/**
	 * Parse the raw bytes array, check if the player already exists, and send back to the client an error code if he cannot join the
	 * server.
	 *
	 * @param identifier The identifier of the client's response.
	 * @param data       The raw bytes array containing the client's response.
	 * 
	 * @returns True if the player can join the server, false otherwise.
	 */
	private boolean handlePlayerProperties(int identifier, byte[] data) {
		PlayerPropertiesRequest payload = parse(data);

		if (payload == null) {
			debug("%s - Technical error happened: Could not parse client's response for player's properties", server);
			return false;
		}

		if (server.getServer().getPlayers().get(payload.getName()) == null) {
			debug("%s - Denying %s, there is no player with this name registered on the voxy server", payload.getName());
			IRequestMessage response = getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_DOES_NOT_EXIST, null);
			response.setSync(true);
			answer(identifier, response);
			return false;
		}

		if (server.getClients().get(payload.getName()) != null) {
			debug("%s - Denying %s, a player with the same name is already registered", server);
			IRequestMessage response = getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_ALREADY_EXIST, null);
			response.setSync(true);
			answer(identifier, response);
			return false;
		}

		playerName = payload.getName();
		answer(identifier, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		return true;
	}
}
