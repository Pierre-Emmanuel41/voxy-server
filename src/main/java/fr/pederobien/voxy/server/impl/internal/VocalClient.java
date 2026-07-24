package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;

import fr.pederobien.messenger.event.ProtocolConnectionUnstableEvent;
import fr.pederobien.messenger.interfaces.IProtocolConnection;
import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.VoxyManagers;
import fr.pederobien.voxy.common.impl.effects.EffectDescription;
import fr.pederobien.voxy.common.impl.effects.EffectManager;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamContentRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamEffectRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamVolumesRequest;
import fr.pederobien.voxy.common.impl.requests.PlayerAudioStreamVolumesRequest.VolumeInfo;
import fr.pederobien.voxy.common.impl.requests.PlayerPropertiesRequest;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent.VolumeChange;

public class VocalClient extends ClientWrapper implements IEventListener {
	private final VocalServer vocalServer;
	private final EffectManager effectManager;
	private VoxyPlayerImpl player;

	/**
	 * Creates a vocal client.
	 * 
	 * @param server The vocal server associated to this vocal client.
	 * @param client The client used to communicate with the remote.
	 */
	protected VocalClient(VocalServer vocalServer, IProtocolClient client) {
		super(vocalServer.getServer(), client);

		this.vocalServer = vocalServer;
		effectManager = VoxyManagers.instance().getEffectManager();

		// Registering event handler
		client.addRequestHandler(VoxyIdentifiers.PLAYER_AUDIO_STREAM_CONTENT, this::onPlayerSpeakEvent);
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
	 * @return The player associated to this vocal client.
	 */
	public VoxyPlayerImpl getPlayer() {
		return player;
	}

	/**
	 * Sends a request to the remote to apply an effect on the audio stream of a player.
	 * 
	 * @param playerName The name of the player on which an effect shall be applied.
	 * @param effectName The name of the effect to apply.
	 * @param values     The effect parameters value.
	 */
	public void setEffect(String playerName, String effectName, Object... values) {
		EffectDescription description = effectManager.getEffectDescription(effectName, values);
		if (description == null) {
			String format = "Cannot retrieve effect description associated to effect name \"%s\" and values %s";
			StringJoiner joiner = new StringJoiner(",", "{", "}");
			for (Object obj : values)
				joiner.add(obj.toString());

			warning(format, effectName, joiner);
			return;
		}

		send(VoxyIdentifiers.PLAYER_AUDIO_STREAM_EFFECT, new PlayerAudioStreamEffectRequest(playerName, description));
	}

	/**
	 * Sends a request to the remote to add the given sample to the audio stream of the given player.
	 * 
	 * @param name      The name of the speaking player.
	 * @param sample    The bytes array that contains the player's audio stream.
	 * @param algorithm The algorithm used to compress the audio stream.
	 */
	public void onPlayerSpeaking(String name, byte[] sample, byte algorithm) {
		send(VoxyIdentifiers.PLAYER_AUDIO_STREAM_CONTENT, new PlayerAudioStreamContentRequest(name, sample, algorithm));
	}

	@EventHandler
	private void onPlayerVolumeChanged(VoxyPlayerVolumesChangedEvent event) {
		for (VolumeChange change : event.getChanges()) {
			if (change.getListener() != player.getExternal())
				continue;

			debug("Notifying %s that volumes has changed for %s", change.getListener().getName(), change.getSpeaker().getName());
			List<VolumeInfo> volumes = new ArrayList<VolumeInfo>();

			String name = change.getSpeaker().getName();
			float left = change.getVolumes().getLeft();
			float right = change.getVolumes().getRight();
			float global = change.getVolumes().getGlobal();
			volumes.add(new VolumeInfo(name, left, right, global));

			send(VoxyIdentifiers.PLAYER_AUDIO_STREAM_VOLUMES, new PlayerAudioStreamVolumesRequest(volumes));
		}
	}

	@EventHandler
	private void onConnectionUnstable(ProtocolConnectionUnstableEvent event) {
		if (event.getConnection() != getClient().getConnection())
			return;

		// Removing player from room
		vocalServer.getRoom().getPlayers().remove(player);
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

		if (vocalServer.getRoom().getPlayers().getByName(payload.getName()) != null) {
			debug("Denying %s, player is already registered", payload.getName());
			IRequestMessage response = createAcknowledgementRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_ALREADY_REGISTERED);
			response.setSync(true);
		}

		VoxyPlayerImpl pending = vocalServer.getRoom().getPlayers().getPendingByName(payload.getName());
		if (pending == null) {
			debug("Denying %s, no player is waiting for joining room %s", payload.getName(), vocalServer.getRoom().getName());
			IRequestMessage response = createAcknowledgementRequest(VoxyIdentifiers.PLAYER_PROPERTIES, VoxyErrors.PLAYER_NOT_REGISTERED);
			response.setSync(true);

			answer(messageID, response);
			return false;
		}

		player = pending;
		player.setVocalClient(this);
		EventManager.registerListener(this);

		debug("Accepting player %s", player.getName());
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
		Logger.debug(3, "%s - %s", this, String.format(format, args));
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void warning(String format, Object... args) {
		Logger.warning("%s - %s", this, String.format(format, args));
	}

	/**
	 * Event handler: Method called when the player is speaking.
	 * 
	 * @param connection The connection with the client.
	 * @param messageID  The client's message identifier.
	 * @param payload    The object that gather properties about player's audio sample.
	 */
	private void onPlayerSpeakEvent(IProtocolConnection connection, int messageID, Object payload) {
		if (!(payload instanceof PlayerAudioStreamContentRequest request))
			return;

		if (!request.getName().equals(player.getName())) {
			debug("Ignoring player's audio sample, the player name is wrong");
			return;
		}

		if (player.isMute()) {
			debug("Ignoring player's audio sample, the player is mute");
			return;
		}

		if (vocalServer.getRoom().getPlayers().getByName(request.getName()) == null) {
			debug("Ignoring player's audio sample, the player is not registered in the room");
			return;
		}

		vocalServer.getRoom().onPlayerIsSpeaking(player, request.getSample(), request.getAlgorithm());
	}
}
