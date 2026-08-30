package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteByChangePostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteByChangePreEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangePostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangePreEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerPlaybackChangePostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerPlaybackChangePreEvent;
import fr.pederobien.voxy.server.impl.VoxyPlayer;
import fr.pederobien.voxy.server.interfaces.ICoordinates;
import fr.pederobien.voxy.server.interfaces.IEffect;
import fr.pederobien.voxy.server.interfaces.ISoundSphere;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerImpl extends ServerElement {
	private final String name;
	private final List<VoxyPlayerImpl> muteByPlayers;
	private final ICoordinates coordinates;
	private final ISoundSphere soundSphere;
	private VocalClient vocalClient;
	private VoxyRoomImpl room;
	private boolean isMute;
	private boolean isDeaf;
	private boolean playback;
	private Object lock;

	private final IVoxyPlayer external;

	/***
	 * Creates the implementation of a VoxyPlayer.
	 *
	 * @param server The server on which the player is connected.
	 * @param name   The player's name.
	 * @param isMute The player's mute status.
	 * @param isDeaf The player's deaf status.
	 */
	public VoxyPlayerImpl(VoxyServerImpl server, String name, boolean isMute, boolean isDeaf) {
		super(server);

		this.name = name;
		this.isMute = isMute;
		this.isDeaf = isDeaf;

		lock = new Object();
		muteByPlayers = new ArrayList<VoxyPlayerImpl>();
		coordinates = new Coordinates(this);
		soundSphere = new SoundSphere(this);
		external = new VoxyPlayer(this);

		playback = false;
	}

	/**
	 * @return The player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Get the voxy room in which the player is. Null if the player is not registered in a room.
	 */
	public VoxyRoomImpl getRoom() {
		return room;
	}

	/**
	 * Set the room in which this player is registered. Null indicates that the player is not registered in a room.
	 * 
	 * @param room The room in which this player is.
	 */
	public void setRoom(VoxyRoomImpl room) {
		this.room = room;
	}

	/**
	 * @return True if the player's microphone is enabled, false if it is disabled.
	 */
	public boolean isMute() {
		return isMute;
	}

	/**
	 * Throws a VoxyPlayerMuteStatusChangePreEvent associated to the given input parameters.
	 * 
	 * @param isMute   The new player's mute status.
	 * @param source   The source that requires a player to change its mute status.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raisePlayerMuteStatusChangePreEvent(boolean isMute, ISource source, Consumer<Boolean> callback) {
		VoxyPlayerMuteStatusChangePreEvent event = new VoxyPlayerMuteStatusChangePreEvent(external, isMute, source);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Set if this player is mute. A VoxyPlayerMuteStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isMute True if this player is mute, false otherwise.
	 */
	public void setMute(boolean isMute) {
		this.isMute = isMute;
		info("Player %s %s itself", name, isMute ? "muted" : "unmuted");
		EventManager.callEvent(new VoxyPlayerMuteStatusChangePostEvent(external, isMute));
	}

	/**
	 * Throws a VoxyPlayerMuteStatusChangePreEvent associated to the given input parameters.
	 * 
	 * @param player   The player that mutes/unmutes another player.
	 * @param isMute   True to mute, false to unmute.
	 * @param source   The source that requires a player to be muted for another player.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raisePlayerMuteByStatusChangePreEvent(IVoxyPlayer player, boolean isMute, ISource source, Consumer<Boolean> callback) {
		VoxyPlayerMuteByChangePreEvent event = new VoxyPlayerMuteByChangePreEvent(player, external, isMute, external);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Set if this player is muted/unmuted by the source player.
	 * 
	 * @param source The player who muted/unmuted this player
	 * @param isMute True if the source player muted this player, false otherwise.
	 */
	public void setMuteBy(VoxyPlayerImpl source, boolean isMute) {
		if (isMute)
			synchronized (lock) {
				muteByPlayers.add(source);
			}
		else
			synchronized (lock) {
				muteByPlayers.remove(source);
			}

		info("Player %s %s player %s", source.getName(), isMute ? "muted" : "unmuted", name);
		EventManager.callEvent(new VoxyPlayerMuteByChangePostEvent(source.getExternal(), external, isMute));
	}

	/**
	 * Check if this player is muted by another player.
	 * 
	 * @param other The player to check.
	 * 
	 * @return True if the other player muted this player, false otherwise.
	 */
	public boolean isMuteBy(VoxyPlayerImpl other) {
		synchronized (lock) {
			return muteByPlayers.contains(other);
		}
	}

	/**
	 * Throws a VoxyPlayerPlaybackChangePreEvent associated to the given input parameters.
	 * 
	 * @param playback The player that mutes/unmutes another player.
	 * @param isMute   True to mute, false to unmute.
	 * @param source   The source that requires a player to be muted for another player.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raisePlayerPlaybackChangePreEvent(boolean playback, ISource source, Consumer<Boolean> callback) {
		VoxyPlayerPlaybackChangePreEvent event = new VoxyPlayerPlaybackChangePreEvent(external, playback, source);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Set if this player shall hear its own audio stream.
	 * 
	 * @param playback True if this player shall hear its own audio stream, false otherwise.
	 */
	public void setPlayback(boolean playback) {
		if (this.playback == playback)
			return;

		this.playback = playback;

		debug("Playback %s for %s", playback ? "enabled" : "disabled", external.getName());
		EventManager.callEvent(new VoxyPlayerPlaybackChangePostEvent(external, playback));
	}

	/**
	 * @return True if this player shall hear its own audio stream, false otherwise.
	 */
	public boolean isPlayback() {
		return playback;
	}

	/**
	 * Set the effect to apply on the audio stream of the speaking player.
	 * 
	 * @param speaker The player that is speaking.
	 * @param index   The index at which the effect shall be added. If the index is greater than the size of the list of effect
	 * @param holder  A holder that contains the effect name and gather effect parameter's name / parameter's value.
	 */
	public void addEffect(IVoxyPlayer speaker, int index, IEffect holder) {
		if (vocalClient == null)
			return;

		vocalClient.addEffect(speaker.getName(), index, holder);
	}

	/**
	 * Stops the effect associated to the given effectName. The effect will transition smoothly from applied to not applied. Once
	 * stopped completely, the effect will be removed.
	 * 
	 * @param name       The name of the audio stream for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffect(IVoxyPlayer speaker, String effectName) {
		if (vocalClient == null)
			return;

		vocalClient.removeEffect(speaker.getName(), effectName);
	}

	/**
	 * Update the parameters of an effect. The parameters defines how the effect modifies the audio stream.
	 * 
	 * @param name   The name of the audio stream on which an effect shall be modified.
	 * @param holder A holder that contains the effect name and gather effect parameter's name / parameter's value.
	 */
	public void updateEffect(IVoxyPlayer speaker, IEffect holder) {
		if (vocalClient == null)
			return;

		vocalClient.updateEffect(speaker.getName(), holder);
	}

	/**
	 * @return True if the player's speakers are enabled, false otherwise.
	 */
	public boolean isDeaf() {
		return isDeaf;
	}

	/**
	 * Set if this player is deaf. A VoxyPlayerDeafStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isDeaf True if this player is deaf, false otherwise.
	 */
	public void setDeaf(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		this.isDeaf = isDeaf;
		info("Player %s %s itself", name, isDeaf ? "deaf" : "undeaf");
		EventManager.callEvent(new VoxyPlayerDeafStatusChangedEvent(external, isDeaf));
	}

	/**
	 * @return The coordinate that represent the player location in game.
	 */
	public ICoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @return The sound sphere associated to this player.
	 */
	public ISoundSphere getSoundSphere() {
		return soundSphere;
	}

	/**
	 * @return The player to be used externally.
	 */
	public IVoxyPlayer getExternal() {
		return external;
	}

	/**
	 * @return The vocal client associated to this player.
	 */
	public VocalClient getVocalClient() {
		return vocalClient;
	}

	/**
	 * Set the vocal client associated to this player.
	 * 
	 * @param vocalClient The client to use to send effect notifications. Can be null when not in a room.
	 */
	public void setVocalClient(VocalClient vocalClient) {
		this.vocalClient = vocalClient;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VoxyPlayerImpl))
			return false;

		VoxyPlayerImpl other = (VoxyPlayerImpl) obj;
		return name.equals(other.getName());
	}
}
