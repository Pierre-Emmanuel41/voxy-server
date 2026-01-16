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
import fr.pederobien.voxy.common.impl.requests.PlayerMuteByRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.JoinRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.event.RenameRoomPrevent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteByChangePreEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangePreEvent;

public class VoxyClientRequestHandler extends ClientWrapper {
	private VoxyPlayerImpl player;

	/**
	 * Creates a request handler to perform getServer() updates from client's side.
	 * 
	 * @param getServer() The getServer() associated to this handler.
	 * @param client      The client that sends requests.
	 */
	public VoxyClientRequestHandler(VoxyServerImpl server, IProtocolClient client) {
		super(server, client);
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
		getClient().addRequestHandler(VoxyIdentifiers.PLAYER_MUTE_BY, this::onPlayerMuteByRequest);
		getClient().addRequestHandler(VoxyIdentifiers.PLAYER_DEAF, this::onPlayerDeafRequest);
	}

	/**
	 * Event handler: Method called when the client requests to add a room to the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to add.
	 */
	private void onAddRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof AddRoomRequest request))
			return;

		debug("%s Sent a request to add room %s", player, request.getName());
		if (getServer().getRooms().getByName(request.getName()) != null) {
			debug("Denying request to add room %s, it already exists", request.getName());
			deny(messageID, VoxyIdentifiers.ADD_ROOM, VoxyErrors.ROOM_ALREADY_REGISTERED);
			return;
		}

		EventManager.callEvent(new AddRoomPreEvent(getServer().getExternal(), request.getName()), isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				debug("The request to add room %s has been cancelled", request.getName());
				cancelled(messageID, VoxyIdentifiers.ADD_ROOM);
			} else {
				debug("Adding room %s to the server", request.getName());
				noError(messageID, VoxyIdentifiers.ADD_ROOM);
				getServer().getRooms().add(request.getName());
			}
		});
	}

	/**
	 * Event handler: Method called when the client requests to remove a room from the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to remove.
	 */
	private void onRemoveRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RemoveRoomRequest request))
			return;

		debug("%s Sent a request to remove room %s", player, request.getName());
		VoxyRoomImpl room = getServer().getRooms().getByName(request.getName());
		if (room == null) {
			debug("%s - Denying request to remove room %s, it does not exist", getServer(), request.getName());
			deny(messageID, VoxyIdentifiers.REMOVE_ROOM, VoxyErrors.ROOM_DOES_NOT_EXIST);
			return;
		}

		EventManager.callEvent(new RemoveRoomPrevent(getServer().getExternal(), room.getExternal()), isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				debug("The request to remove room %s has been cancelled", request.getName());
				cancelled(messageID, VoxyIdentifiers.REMOVE_ROOM);
			} else {
				debug("Removing room %s from the server", request.getName());
				noError(messageID, VoxyIdentifiers.REMOVE_ROOM);
				getServer().getRooms().remove(room);
			}
		});
	}

	/**
	 * Event handler: Method called when the client requests to rename a room on the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to rename.
	 */
	private void onRenameRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof RenameRoomRequest request))
			return;

		debug("%s Sent a request to rename room %s as %s", player, request.getOldName(), request.getNewName());
		VoxyRoomImpl room = getServer().getRooms().getByName(request.getOldName());
		if (room == null) {
			debug("Denying request to rename room %s, it does not exist", request.getOldName());
			deny(messageID, VoxyIdentifiers.RENAME_ROOM, VoxyErrors.ROOM_DOES_NOT_EXIST);
			return;
		}

		if (getServer().getRooms().getByName(request.getNewName()) != null) {
			debug("Denying request to rename room %s as %s, the new room already exists", request.getOldName(), request.getNewName());
			deny(messageID, VoxyIdentifiers.RENAME_ROOM, VoxyErrors.ROOM_ALREADY_REGISTERED);
			return;
		}

		EventManager.callEvent(new RenameRoomPrevent(room.getExternal(), request.getNewName()), isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				debug("The request to remove room %s has been cancelled", request.getNewName());
				cancelled(messageID, VoxyIdentifiers.RENAME_ROOM);
			} else {
				debug("Renaming room %s as %s", request.getOldName(), request.getNewName());
				noError(messageID, VoxyIdentifiers.RENAME_ROOM);
				room.setName(request.getNewName());
			}
		});
	}

	/**
	 * Event handler: Method called when the client requests to join a room on the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to join.
	 */
	private void onJoinRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof JoinRoomRequest request))
			return;

		debug("%s Sent a request to join room %s", player, request.getRoomName());
		if (!request.getPlayerName().equals(player.getName())) {
			debug("Denying the request to join room %s, the player's name is wrong", request.getPlayerName());
			deny(messageID, VoxyIdentifiers.JOIN_ROOM, VoxyErrors.PLAYER_NAME_INCORRECT);
			return;
		}

		VoxyRoomImpl room = getServer().getRooms().getByName(request.getRoomName());
		if (room == null) {
			debug("Denying request to join room %s, it does not exist", request.getRoomName());
			deny(messageID, VoxyIdentifiers.JOIN_ROOM, VoxyErrors.ROOM_DOES_NOT_EXIST);
			return;
		}

		if (room.getPlayers().getByName(request.getPlayerName()) != null) {
			debug("Denying request to join room %s, the player is already registered", request.getRoomName());
			deny(messageID, VoxyIdentifiers.JOIN_ROOM, VoxyErrors.PLAYER_ALREADY_REGISTERED);
			return;
		}

		// Checking if player is already registered
		getServer().getRooms().foreach(toCheck -> {
			VoxyPlayerImpl player = toCheck.getPlayers().getByName(request.getPlayerName());
			if (player != null)
				toCheck.getPlayers().remove(player);
		});

		EventManager.callEvent(new JoinRoomPreEvent(room.getExternal(), player.getExternal()), isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				debug("The request to join room %s has been cancelled", request.getRoomName());
				cancelled(messageID, VoxyIdentifiers.JOIN_ROOM);
			} else {
				debug("Player %s is joining room %s", request.getPlayerName(), request.getRoomName());
				noError(messageID, VoxyIdentifiers.JOIN_ROOM);
				room.getPlayers().addPending(player);
			}
		});
	}

	/**
	 * Event handler: Method called when the client requests to join a room on the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the room to join.
	 */
	private void onLeaveRoomRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof LeaveRoomRequest request))
			return;

		debug("%s Sent a request to leave room %s", player, request.getRoomName());
		if (!request.getPlayerName().equals(player.getName())) {
			debug("Denying the request to leave room %s, the player's name is wrong", request.getPlayerName());
			deny(messageID, VoxyIdentifiers.LEAVE_ROOM, VoxyErrors.PLAYER_NAME_INCORRECT);
			return;
		}

		VoxyRoomImpl room = getServer().getRooms().getByName(request.getRoomName());
		if (room == null) {
			debug("Denying request to leave room %s, it does not exist", request.getRoomName());
			deny(messageID, VoxyIdentifiers.LEAVE_ROOM, VoxyErrors.ROOM_DOES_NOT_EXIST);
			return;
		}

		if (room.getPlayers().getByName(request.getPlayerName()) == null) {
			debug("Denying request to leave room %s, the player is not registered in this room", request.getRoomName());
			deny(messageID, VoxyIdentifiers.LEAVE_ROOM, VoxyErrors.PLAYER_NOT_REGISTERED);
			return;
		}

		debug("Removing player %s from room %s", request.getPlayerName(), request.getRoomName());
		noError(messageID, VoxyIdentifiers.LEAVE_ROOM);

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

		debug("%s Notified the server that its mute status has changed, isMute=%s", player, request.isMute());
		if (!request.getName().equals(player.getName())) {
			debug("Denying request, the player's name is wrong", request.getName());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE, VoxyErrors.PLAYER_NAME_INCORRECT);
			return;
		}

		EventManager.callEvent(new VoxyPlayerMuteStatusChangePreEvent(player.getExternal(), request.isMute()), isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				debug("The request to %s player %s has been cancelled", request.isMute() ? "mute" : "unmute", request.getName());
				cancelled(messageID, VoxyIdentifiers.PLAYER_MUTE);
			} else {
				debug("Updating player's mute status");
				noError(messageID, VoxyIdentifiers.PLAYER_MUTE);
				player.setMute(request.isMute());
			}
		});
	}

	/**
	 * Event handler: Method called when the client mutes/unmutes another player.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the source player, the target player and the mute status.
	 */
	private void onPlayerMuteByRequest(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerMuteByRequest request))
			return;

		debug("%s Notified the server that it %s player %s", player, request.isMute() ? "muted" : "unmuted", request.getTarget());
		if (!request.getSource().equals(player.getName())) {
			debug("Denying request, the player's name is wrong", request.getSource());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE, VoxyErrors.PLAYER_NAME_INCORRECT);
			return;
		}

		VoxyRoomImpl room = getServer().getRooms().getRoomByPlayerName(player.getName());
		if (room == null) {
			debug("Denying request, the player %s is not registered in a room", player.getName());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE_BY, VoxyErrors.PLAYER_NOT_REGISTERED);
			return;
		}

		VoxyPlayerImpl target = room.getPlayers().getByName(request.getTarget());
		if (target == null) {
			debug("Denying request, the player %s is not registered in the same room", request.getTarget());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE_BY, VoxyErrors.PLAYER_NOT_REGISTERED);
			return;
		}

		if (request.isMute() && target.isMuteBy(player)) {
			debug("Denying request, the player %s already muted player %s", request.getSource(), request.getTarget());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE_BY, VoxyErrors.PLAYER_ALREADY_MUTED);
			return;
		}

		if (!request.isMute() && !target.isMuteBy(player)) {
			debug("Denying request, the player %s did not mute player %s", request.getSource(), request.getTarget());
			deny(messageID, VoxyIdentifiers.PLAYER_MUTE_BY, VoxyErrors.PLAYER_NOT_MUTED);
			return;
		}

		VoxyPlayerMuteByChangePreEvent preEvent = new VoxyPlayerMuteByChangePreEvent(player.getExternal(), target.getExternal(), request.isMute());
		EventManager.callEvent(preEvent, isCancelled -> {
			// Notifying the client that the request has been cancelled
			if (isCancelled) {
				String format = "The request to %s player %s by player %s has been cancelled";
				debug(format, request.isMute() ? "mute" : "unmute", request.getTarget(), request.getSource());
				cancelled(messageID, VoxyIdentifiers.PLAYER_MUTE_BY);
			} else {
				debug("Updating player's mute by status");
				noError(messageID, VoxyIdentifiers.PLAYER_MUTE_BY);
				target.setMuteBy(player, request.isMute());
			}
		});
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

		debug("%s Notified the server that its deaf status has changed, isDeaf=%s", player, request.isDeaf());
		if (!request.getName().equals(player.getName())) {
			debug("Ignoring request, the player's name is wrong", request.getName());
			deny(messageID, VoxyIdentifiers.PLAYER_DEAF, VoxyErrors.PLAYER_NAME_INCORRECT);
			return;
		}

		debug("Updating player's deaf status");
		player.setDeaf(request.isDeaf());
	}
}
