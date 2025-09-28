package fr.pederobien.voxy.server.impl;

import java.util.function.Consumer;

import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyClient implements IEventListener {
	private final VoxyServer server;
	private final IProtocolClient client;
	private VoxyPlayer player;

	/***
	 * Creates a client used to send requests to the remote.
	 *
	 * @param server The server with which this client is connected.
	 * @param client The client that gather requests that can be sent to the remote.
	 */
	public VoxyClient(VoxyServer server, IProtocolClient client) {
		this.server = server;
		this.client = client;
	}

	/**
	 * @return The player associated to this client.
	 */
	public IVoxyPlayer getPlayer() {
		return player;
	}

	/**
	 * Sends a request to the remote to get player's properties.
	 *
	 * @param callback The code to execute when the initialization is finished. The input parameter is the initialization status.
	 */
	public void initialize(Consumer<Boolean> callback) {
		// Sending request to get player's properties
		IRequestMessage request = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, new PlayerPropertiesRequest());
		request.setCallback(args -> {
			boolean success = false;
			Logger.debug("Timeout: %s", args.isTimeout());
			if (!args.isTimeout() && handlePlayerProperties(args.identifier(), args.response())) {
				success = true;
				EventManager.registerListener(this);
			}

			callback.accept(success);
		});

		send(request);
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		client.dispose();
	}

	@EventHandler
	private void onRoomAdded(AddRoomPostEvent event) {
		if (event.getServer() != server)
			return;

		// Notifying the remote that a room has been added
		send(getRequest(VoxyIdentifiers.ADD_ROOM, new AddRoomRequest(event.getRoom().getName(), event.getRoom().getPort())));
	}

	@EventHandler
	private void onRoomRemoved(RemoveRoomPrevent event) {
		if (event.getServer() != server)
			return;

		// Notifying the remote that a room has been removed
		send(getRequest(VoxyIdentifiers.REMOVE_ROOM, new RemoveRoomRequest(event.getRoom().getName())));
	}

	@EventHandler
	private void onRoomRenamed(RenameRoomPostEvent event) {
		if (event.getRoom().getServer() != server)
			return;

		// Notifying the remote that a room has been renamed
		send(getRequest(VoxyIdentifiers.RENAME_ROOM, new RenameRoomRequest(event.getOldName(), event.getRoom().getName())));
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
			// Technical error happened
			return false;
		}

		boolean isRegistered = true/* server.getPlayers().get(payload.getName()) != null */;
		if (isRegistered) {
			// A player with the same name already exists
			IRequestMessage response = getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_ALREADY_EXIST, null);
			response.setSync(true);
			answer(identifier, response);
			return false;
		}

		player = new VoxyPlayer(server, payload.getName(), payload.isMute(), payload.isDeaf());
		answer(identifier, getRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.NO_ERROR, null));
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
	private IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
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
	private IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param request The request to send to the remote.
	 */
	private void send(IRequestMessage request) {
		client.getConnection().send(request);
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	private void answer(int messageID, IRequestMessage request) {
		client.getConnection().answer(messageID, request);
	}

	/**
	 * Parse the given bytes array to get the payload.
	 *
	 * @param data The raw bytes array to parse.
	 * @return The payload parsed, or null if a ClassCastException occurred.
	 */
	@SuppressWarnings("unchecked")
	private <T> T parse(byte[] data) {
		try {
			return (T) client.parse(data).getPayload();
		} catch (ClassCastException e) {
			return null;
		}
	}
}
