package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.messenger.event.ProtocolConnectionLostEvent;
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
import fr.pederobien.voxy.common.impl.requests.LeaveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerDeafRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerMuteRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyClientNotifier extends ClientWrapper implements IEventListener {
	private VoxyPlayerImpl player;

	/**
	 * Creates a notifier to send updates to the remote client.
	 * 
	 * @param getServer() The getServer() implementation associated to this notifier.
	 * @param source      The client to notify.
	 */
	public VoxyClientNotifier(VoxyServerImpl server, IProtocolClient client) {
		super(server, client);

		// Registering event handler
		getClient().addRequestHandler(VoxyIdentifiers.SERVER_PROPERTIES, this::onServerPropertiesRequest);
	}

	/**
	 * Sends a request to the remote to get player's properties.
	 *
	 * @param callback The code to execute when the initialization is finished. The input parameter is the initialization status.
	 */
	protected void initialize(Consumer<Boolean> callback) {
		// Sending request to get player's properties
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
	 * @return The player associated to this client.
	 */
	protected VoxyPlayerImpl getPlayer() {
		return player;
	}

	@EventHandler
	private void onRoomAdded(AddRoomPostEvent event) {
		if (event.getServer() != getServer().getExternal())
			return;

		VoxyRoomImpl room = getServer().getRooms().getByName(event.getRoom().getName());

		// Notifying the remote that a room has been added
		send(VoxyIdentifiers.ADD_ROOM, new AddRoomRequest(room.getName(), room.getVocalServer().getPort()));
	}

	@EventHandler
	private void onRoomRemoved(RemoveRoomPostEvent event) {
		if (event.getServer() != getServer().getExternal())
			return;

		// Notifying the remote that a room has been removed
		send(VoxyIdentifiers.REMOVE_ROOM, new RemoveRoomRequest(event.getRoom().getName()));
	}

	@EventHandler
	private void onRoomRenamed(RenameRoomPostEvent event) {
		if (event.getRoom().getServer() != getServer().getExternal())
			return;

		// Notifying the remote that a room has been renamed
		send(VoxyIdentifiers.RENAME_ROOM, new RenameRoomRequest(event.getOldName(), event.getRoom().getName()));
	}

	@EventHandler
	private void onPlayerJoinedRoom(JoinRoomPostEvent event) {
		if (event.getRoom().getServer() != getServer().getExternal())
			return;

		// Notifying the remote a player joined a room
		IVoxyPlayer player = event.getPlayer();
		send(VoxyIdentifiers.JOIN_ROOM, new JoinRoomRequest(event.getRoom().getName(), player.getName(), player.isMute(), player.isDeaf()));
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom().getServer() != getServer().getExternal())
			return;

		// Notifying the remote a player left a room
		send(VoxyIdentifiers.LEAVE_ROOM, new LeaveRoomRequest(event.getRoom().getName(), event.getPlayer().getName()));
	}

	@EventHandler
	private void onPlayerMuteStatusChanged(VoxyPlayerMuteStatusChangedEvent event) {
		if (event.getPlayer().getServer() != getServer().getExternal())
			return;

		// Notifying the remote a player muted/unmuted itself
		send(VoxyIdentifiers.PLAYER_MUTE, new PlayerMuteRequest(event.getPlayer().getName(), event.isMute()));
	}

	@EventHandler
	private void onPlayerDeafStatusChanged(VoxyPlayerDeafStatusChangedEvent event) {
		if (event.getPlayer().getServer() != getServer().getExternal())
			return;

		// Notifying the remote a player deaf/undeaf itself
		send(VoxyIdentifiers.PLAYER_DEAF, new PlayerDeafRequest(event.getPlayer().getName(), event.isDeaf()));
	}

	@EventHandler
	private void onConnectionLost(ProtocolConnectionLostEvent event) {
		if (event.getConnection() != getClient().getConnection())
			return;

		info("Connection lost with %s", player.getName());

		// Unregistering from events
		EventManager.unregisterListener(this);

		debug("Notifying each room to remove player %s", player.getName());

		// Removing the player from room if registered in a room
		for (IVoxyRoom room : getServer().getRooms().toList())
			room.getPlayers().remove(player.getName());
	}

	/**
	 * Parse the raw bytes array, check if the player already exists, and send back to the client an error code if he cannot join the
	 * getServer().
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param data      The raw bytes array containing the client's response.
	 * 
	 * @returns True if the player can join the getServer(), false otherwise.
	 */
	private boolean handlePlayerProperties(int messageID, byte[] data) {
		PlayerPropertiesRequest payload = parse(data);

		if (payload == null) {
			debug("%s - Technical error happened: Could not parse client's response for player's properties", getServer());
			return false;
		}

		if (getServer().getPlayerByName(payload.getName()) != null) {
			debug("%s - Denying %s, a player with the same name is already registered", getServer());
			IRequestMessage response = createAcknowledgementRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_ALREADY_EXIST);
			response.setSync(true);

			answer(messageID, response);
			return false;
		}

		player = new VoxyPlayerImpl(getServer(), payload.getName(), payload.isMute(), payload.isDeaf());

		debug("Accepting player properties: name=%s, isMute=%s, isDeaf=%s", player.getName(), player.isMute(), player.isDeaf());
		noError(messageID, VoxyIdentifiers.PLAYER_PROPERTIES);
		return true;
	}

	/**
	 * Event handler: Method called when the client requests the properties of the getServer().
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about the getServer().
	 */
	private void onServerPropertiesRequest(IProtocolConnection connection, int messageID, Object ignored) {
		debug("%s requires server's properties", player);

		List<RoomInfo> rooms = new ArrayList<RoomInfo>();
		getServer().getRooms().foreach(room -> {
			List<PlayerInfo> players = new ArrayList<PlayerInfo>();

			room.getPlayers().foreach(player -> {
				players.add(new PlayerInfo(player.getName(), player.isMute(), player.isDeaf()));
			});

			rooms.add(new RoomInfo(room.getName(), room.getVocalServer().getPort(), players));
		});

		ServerPropertiesRequest payload = new ServerPropertiesRequest(rooms);
		debug("Sending following payload: %s", payload);
		answer(messageID, getRequest(VoxyIdentifiers.SERVER_PROPERTIES, payload));
	}
}
