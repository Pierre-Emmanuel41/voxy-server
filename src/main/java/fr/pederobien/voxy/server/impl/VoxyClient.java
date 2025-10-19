package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.JoinRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyClient extends ClientWrapper implements IEventListener {
	private final VoxyServer server;
	private VoxyPlayer player;

	/***
	 * Creates a client used to send requests to the remote.
	 *
	 * @param server The server with which this client is connected.
	 * @param client The client that gather requests that can be sent to the remote.
	 */
	public VoxyClient(VoxyServer server, IProtocolClient client) {
		super(client);
		this.server = server;

		// Registering event handler
		client.addRequestHandler(VoxyIdentifiers.SERVER_PROPERTIES, this::onServerPropertiesRequest);
		client.addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onAddRoomRequest);
		client.addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRemoveRoomRequest);
		client.addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRenameRoomRequest);
		client.addRequestHandler(VoxyIdentifiers.JOIN_ROOM, this::onJoinRoomRequest);
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

	@Override
	public String toString() {
		return getClient().toString();
	}

	/**
	 * Close definitely this client, it cannot be reused to send or received data with the remote.
	 */
	public void dispose() {
		getClient().dispose();
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

	@EventHandler
	private void onPlayerJoinedRoom(JoinRoomPostEvent event) {
		if (event.getRoom().getServer() != server)
			return;

		JoinRoomRequest request = new JoinRoomRequest(event.getRoom().getName(), event.getPlayer().getName(), event.getPlayer().isMute(), event.getPlayer().isDeaf());
		send(getRequest(VoxyIdentifiers.JOIN_ROOM, request));
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

		if (server.getPlayers().get(payload.getName()) != null) {
			debug("%s - Denying %s, a player with the same name is already registered", server);
			IRequestMessage response = getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_ALREADY_EXIST, null);
			response.setSync(true);
			answer(identifier, response);
			return false;
		}

		player = new VoxyPlayer(server, payload.getName(), payload.isMute(), payload.isDeaf());
		answer(identifier, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		return true;
	}

	/**
	 * Event handler: Method called when the client requests the properties of the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the server.
	 */
	private void onServerPropertiesRequest(IProtocolConnection connection, int messageID, Object ignored) {
		debug("%s - requires server's properties", player);

		List<RoomInfo> rooms = new ArrayList<RoomInfo>();
		for (IVoxyRoom room : server.getRooms().values()) {

			List<PlayerInfo> players = new ArrayList<PlayerInfo>();
			for (IVoxyPlayer player : room.getPlayers().values())
				players.add(new PlayerInfo(player.getName(), player.isMute(), player.isDeaf()));

			rooms.add(new RoomInfo(room.getName(), room.getPort(), players));
		}

		ServerPropertiesRequest payload = new ServerPropertiesRequest(rooms);
		debug("%s - Sending following payload: %s", server, payload);
		answer(messageID, getRequest(VoxyIdentifiers.SERVER_PROPERTIES, payload));
	}

	/**
	 * Event handler: Method called when the client requests to add a room to the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to add.
	 */
	private void onAddRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof AddRoomRequest request))
			return;

		debug("%s - Sent a request to add room %s", player, request.getName());
		if (server.getRooms().get(request.getName()) != null) {
			debug("%s - Denying request to add room %s, it already exists", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_ALREADY_REGISTERED, null));
			return;
		}

		debug("%s - Accepting request to add room %s", server, request.getName());
		answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		server.add(request.getName());
	}

	/**
	 * Event handler: Method called when the client requests to remove a room from the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to remove.
	 */
	private void onRemoveRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RemoveRoomRequest request))
			return;

		debug("%s - Sent a request to remove room %s", player, request.getName());
		if (server.getRooms().get(request.getName()) == null) {
			debug("%s - Denying request to remove room %s, it does not exist", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		debug("%s - Accepting request to remove room %s", server, request.getName());
		answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		server.remove(request.getName());
	}

	/**
	 * Event handler: Method called when the client requests to rename a room on the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to rename.
	 */
	private void onRenameRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RenameRoomRequest request))
			return;

		debug("%s - Sent a request to rename room %s as %s", player, request.getOldName(), request.getNewName());
		IVoxyRoom room = server.getRooms().get(request.getOldName());
		if (room == null) {
			debug("%s - Denying request to rename room %s, it does not exist", server, request.getOldName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		if (server.getRooms().get(request.getNewName()) != null) {
			debug("%s - Denying request to rename room %s as %s, the new room already exists", server, request.getOldName(), request.getNewName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_ALREADY_REGISTERED, null));
			return;
		}

		debug("%s - Accepting request to rename room %s as %s", server, request.getOldName(), request.getNewName());
		answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		room.setName(request.getNewName());
	}

	/**
	 * Event handler: Method called when the client requests to join a room on the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to join.
	 */
	private void onJoinRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof JoinRoomRequest request))
			return;

		debug("%s - Sent a request to join room %s", player, request.getRoomName());
		if (!request.getPlayerName().equals(player.getName())) {
			debug("%s - Denying the request to join room %s, the player's name is wrong", server, request.getPlayerName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NAME_INCORRECT, null));
			return;
		}

		IVoxyRoom room = server.getRooms().get(request.getRoomName());
		if (room == null) {
			debug("%s - Denying request to join room %s, it does not exist", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		if (room.getPlayers().get(request.getPlayerName()) != null) {
			debug("%s - Denying request to join room %s, the player is already registered", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_ALREADY_REGISTERED, null));
			return;
		}

		debug("%s - Accepting request to join room %s", server, request.getRoomName());
		answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));
		room.add(player);
	}
}
