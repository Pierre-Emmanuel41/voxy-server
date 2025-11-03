package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AddRoomRequest;
import fr.pederobien.voxy.common.impl.requests.JoinRoomRequest;
import fr.pederobien.voxy.common.impl.requests.LeaveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerDeafRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.JoinRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.event.RenameRoomPrevent;

public class VoxyClientRequestHandler extends ClientWrapper {
	private final VoxyServerImpl server;
	private VoxyPlayerImpl player;

	/**
	 * Creates a request handler to perform server updates from client's side.
	 * 
	 * @param server The server associated to this handler.
	 * @param client The client that sends requests.
	 */
	public VoxyClientRequestHandler(VoxyServerImpl server, IProtocolClient client) {
		super(client);

		this.server = server;
	}

	/**
	 * Initialize this request handler
	 * 
	 * @param player The player associated to this handler.
	 */
	protected void initialize(VoxyPlayerImpl player) {
		this.player = player;

		// Registering event handler
		getClient().addRequestHandler(VoxyIdentifiers.ADD_ROOM, this::onAddRoomRequest);
		getClient().addRequestHandler(VoxyIdentifiers.REMOVE_ROOM, this::onRemoveRoomRequest);
		getClient().addRequestHandler(VoxyIdentifiers.RENAME_ROOM, this::onRenameRoomRequest);
		getClient().addRequestHandler(VoxyIdentifiers.JOIN_ROOM, this::onJoinRoomRequest);
		getClient().addRequestHandler(VoxyIdentifiers.LEAVE_ROOM, this::onLeaveRoomRequest);
		getClient().addRequestHandler(VoxyIdentifiers.PLAYER_MUTE, this::onPlayerMuteRequest);
		getClient().addRequestHandler(VoxyIdentifiers.PLAYER_DEAF, this::onPlayerDeafRequest);
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
		if (server.getRooms().getByName(request.getName()) != null) {
			debug("%s - Denying request to add room %s, it already exists", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_ALREADY_REGISTERED, null));
			return;
		}

		AddRoomPreEvent preEvent = new AddRoomPreEvent(server.getExternal(), request.getName());
		EventManager.callEvent(preEvent);

		// Notifying the client that the request has been cancelled
		if (preEvent.isCancelled()) {
			debug("%s - The request to add room %s has been cancelled", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.CANCELLED, null));
		} else {
			debug("%s - Adding room %s to the server", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));

			server.getRooms().add(request.getName());
		}
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
		VoxyRoomImpl room = server.getRooms().getByName(request.getName());
		if (room == null) {
			debug("%s - Denying request to remove room %s, it does not exist", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		RemoveRoomPrevent preEvent = new RemoveRoomPrevent(server.getExternal(), room.getExternal());
		EventManager.callEvent(preEvent);

		// Notifying the client that the request has been cancelled
		if (preEvent.isCancelled()) {
			debug("%s - The request to remove room %s has been cancelled", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.CANCELLED, null));
		} else {
			debug("%s - Removing room %s from the server", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));

			server.getRooms().remove(room);
		}
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
		VoxyRoomImpl room = server.getRooms().getByName(request.getOldName());
		if (room == null) {
			debug("%s - Denying request to rename room %s, it does not exist", server, request.getOldName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		if (server.getRooms().getByName(request.getNewName()) != null) {
			debug("%s - Denying request to rename room %s as %s, the new room already exists", server, request.getOldName(), request.getNewName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_ALREADY_REGISTERED, null));
			return;
		}

		RenameRoomPrevent preEvent = new RenameRoomPrevent(room.getExternal(), request.getNewName());
		EventManager.callEvent(preEvent);

		// Notifying the client that the request has been cancelled
		if (preEvent.isCancelled()) {
			debug("%s - The request to remove room %s has been cancelled", server, request.getNewName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.CANCELLED, null));
		} else {
			debug("%s - Renaming room %s as %s", server, request.getOldName(), request.getNewName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));

			room.setName(request.getNewName());
		}
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

		VoxyRoomImpl room = server.getRooms().getByName(request.getRoomName());
		if (room == null) {
			debug("%s - Denying request to join room %s, it does not exist", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		if (room.getPlayers().getByName(request.getPlayerName()) != null) {
			debug("%s - Denying request to join room %s, the player is already registered", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_ALREADY_REGISTERED, null));
			return;
		}

		// Checking if player is already registered
		for (VoxyRoomImpl toCheck : server.getRooms().get()) {
			VoxyPlayerImpl player = toCheck.getPlayers().getByName(request.getPlayerName());
			if (player != null)
				toCheck.getPlayers().remove(player);
		}

		JoinRoomPreEvent preEvent = new JoinRoomPreEvent(room.getExternal(), player.getExternal());
		EventManager.callEvent(preEvent);

		// Notifying the client that the request has been cancelled
		if (preEvent.isCancelled()) {
			debug("%s - The request to join room %s has been cancelled", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.CANCELLED, null));
		} else {
			debug("%s - Player %s is joining room %s", server, request.getPlayerName(), request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));

			room.getPlayers().add(player);
		}
	}

	/**
	 * Event handler: Method called when the client requests to join a room on the server.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to join.
	 */
	private void onLeaveRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof LeaveRoomRequest request))
			return;

		debug("%s - Sent a request to leave room %s", player, request.getRoomName());
		if (!request.getPlayerName().equals(player.getName())) {
			debug("%s - Denying the request to leave room %s, the player's name is wrong", server, request.getPlayerName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NAME_INCORRECT, null));
			return;
		}

		VoxyRoomImpl room = server.getRooms().getByName(request.getRoomName());
		if (room == null) {
			debug("%s - Denying request to leave room %s, it does not exist", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.ROOM_DOES_NOT_EXIST, null));
			return;
		}

		if (room.getPlayers().getByName(request.getPlayerName()) == null) {
			debug("%s - Denying request to leave room %s, the player is not registered in this room", server, request.getRoomName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NOT_REGISTERED, null));
			return;
		}

		debug("%s - Removing player %s from room %s", server, request.getPlayerName(), request.getRoomName());
		answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.NO_ERROR, null));

		room.getPlayers().remove(player);
	}

	/**
	 * Event handler: Method called when the client mutes/unmutes itself.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the player and the mute status.
	 */
	private void onPlayerMuteRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerMuteRequest request))
			return;

		debug("%s - Notified the server that its mute status has changed, isMute=%s", player, request.isMute());
		if (!request.getName().equals(player.getName())) {
			debug("%s - Ignoring request, the player's name is wrong", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NAME_INCORRECT, null));
			return;
		}

		debug("Updating player's mute status");
		player.setMute(request.isMute());
	}

	/**
	 * Event handler: Method called when the client deaf/undeaf itself.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the player and the deaf status.
	 */
	private void onPlayerDeafRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerDeafRequest request))
			return;

		debug("%s - Notified the server that its deaf status has changed, isDeaf=%s", player, request.isDeaf());
		if (!request.getName().equals(player.getName())) {
			debug("%s - Ignoring request, the player's name is wrong", server, request.getName());
			answer(messageID, getRequest(VoxyIdentifiers.ACKOWLEDGEMENT, VoxyErrors.PLAYER_NAME_INCORRECT, null));
			return;
		}

		debug("Updating player's deaf status");
		player.setDeaf(request.isDeaf());
	}
}
