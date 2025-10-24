package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.messenger.event.ProtocolConnectionDisposedEvent;
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
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.RemoveRoomRequest;
import fr.pederobien.voxy.common.impl.requests.RenameRoomRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.PlayerInfo;
import fr.pederobien.voxy.common.impl.requests.ServerPropertiesRequest.RoomInfo;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyClientNotifier extends ClientWrapper implements IEventListener {
	private final VoxyServer server;
	private VoxyPlayer player;

	/**
	 * Creates a notifier to send updates to the client.
	 * 
	 * @param server The server associated to this notifier.
	 * @param source The client to notify.
	 */
	public VoxyClientNotifier(VoxyServer server, IProtocolClient client) {
		super(client);

		this.server = server;

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
	 * @return The player associated to this client.
	 */
	protected VoxyPlayer getPlayer() {
		return player;
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

		// Notifying the remote a player joined a room
		JoinRoomRequest request = new JoinRoomRequest(event.getRoom().getName(), event.getPlayer().getName(), event.getPlayer().isMute(), event.getPlayer().isDeaf());
		send(getRequest(VoxyIdentifiers.JOIN_ROOM, request));
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom().getServer() != server)
			return;

		// Notifying the remote a player left a room
		LeaveRoomRequest request = new LeaveRoomRequest(event.getRoom().getName(), event.getPlayer().getName());
		send(getRequest(VoxyIdentifiers.LEAVE_ROOM, request));
	}

	@EventHandler
	private void onConnectionDisposed(ProtocolConnectionDisposedEvent event) {
		if (event.getConnection() != getClient().getConnection())
			return;

		info("%s - Connection disposed with %s", server, player.getName());

		// Unregistering from events
		EventManager.unregisterListener(this);

		debug("Notifying each room to remove player %s", player.getName());

		// Removing the player from room if registered in a room
		for (IVoxyRoom room : server.getRooms().toList())
			room.getPlayers().remove(player.getName());
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

		debug("%s - Accepting player properties: name=%s, isMute=%s, isDeaf=%s", server, player.getName(), player.isMute(), player.isDeaf());
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
		for (IVoxyRoom room : server.getRooms().toList()) {

			List<PlayerInfo> players = new ArrayList<PlayerInfo>();
			for (IVoxyPlayer player : room.getPlayers().toList())
				players.add(new PlayerInfo(player.getName(), player.isMute(), player.isDeaf()));

			rooms.add(new RoomInfo(room.getName(), room.getPort(), players));
		}

		ServerPropertiesRequest payload = new ServerPropertiesRequest(rooms);
		debug("%s - Sending following payload: %s", server, payload);
		answer(messageID, getRequest(VoxyIdentifiers.SERVER_PROPERTIES, payload));
	}
}
