package fr.pederobien.voxy.server.impl.internal;

import java.util.function.Consumer;

import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;

public class VocalClient extends ClientWrapper {
	private final VocalServer vocalServer;
	private String playerName;

	/**
	 * Creates a vocal client.
	 * 
	 * @param server The vocal server associated to this vocal client.
	 * @param client The client used to communicate with the remote.
	 */
	protected VocalClient(VocalServer vocalServer, IProtocolClient client) {
		super(vocalServer.getServer(), client);

		this.vocalServer = vocalServer;
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
		debug("Requiring player's properties");
		IRequestMessage request = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, new PlayerPropertiesRequest());
		request.setCallback(args -> {
			boolean success = false;
			if (!args.isTimeout() && handlePlayerProperties(args.identifier(), args.response())) {
				success = true;
			}

			callback.accept(success);
		});

		send(request);
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		getClient().dispose();
	}

	/**
	 * @return The name of the player.
	 */
	public String getPlayerName() {
		return playerName;
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
		PlayerPropertiesRequest payload = parse(data);

		if (payload == null) {
			debug("Technical error happened: Could not parse client's response for player's properties");
			return false;
		}

		if (!getServer().isRegistered(payload.getName())) {
			debug("Denying %s, there is no player with this name registered on the voxy server", payload.getName());
			IRequestMessage response = createAcknowledgementRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_DOES_NOT_EXIST);
			response.setSync(true);

			answer(messageID, response);
			return false;
		}

		if (vocalServer.getRoom().getPlayers().getByName(payload.getName()) == null) {
			debug("Denying %s, no player is registered", payload.getName());
			IRequestMessage response = createAcknowledgementRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_NOT_REGISTERED);
			response.setSync(true);

			answer(messageID, response);
			return false;
		}

		playerName = payload.getName();

		debug("Accepting player %s", playerName);
		noError(messageID, VoxyIdentifiers.PLAYER_PROPERTIES);
		return true;
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s - %s", this, String.format(format, args));
	}
}
