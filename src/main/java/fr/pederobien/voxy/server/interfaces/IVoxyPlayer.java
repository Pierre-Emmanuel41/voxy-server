package fr.pederobien.voxy.server.interfaces;

public interface IVoxyPlayer extends ISource {

	/**
	 * @return The server on which the player is connected.
	 */
	IVoxyServer getServer();

	/**
	 * @return The player's name
	 */
	String getName();

	/**
	 * @return Get the voxy room in which the player is. Null if the player is not registered in a room.
	 */
	IVoxyRoom getRoom();

	/**
	 * @return True if the player is muted, false otherwise.
	 */
	boolean isMute();

	/**
	 * Set if this player is mute. A VoxyPlayerMuteStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isMute True if this player is mute, false otherwise.
	 * @param source The source that requires this player to change its mute status.
	 * 
	 * @return True if the mute status has been updated, false otherwise.
	 */
	boolean setMute(boolean isMute, ISource source);

	/**
	 * Set if this player is mute for another player.
	 * 
	 * @param isMute True if this player is mute for the given player, false otherwise.
	 * @param player The player for which this player is mute / unmute.
	 * @param source The source that requires this player to change its mute status for the given player.
	 * @return True if the mute status has been updated for the given player, false otherwise.
	 */
	boolean setMuteBy(boolean isMute, IVoxyPlayer player, ISource source);

	/**
	 * Set if this player shall hear it's own audio stream.
	 * 
	 * @param playback True if this player shall hear it's own audio stream, false otherwise.
	 * @param source   The source that requires this player to change its playback status.
	 * @return True if the playback status has been updated, false otherwise.
	 */
	boolean setPlayback(boolean playback, ISource source);

	/**
	 * @return True if this player shall hear it's own audio stream, false otherwise.
	 */
	boolean isPlayback();

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	boolean isDeaf();

	/**
	 * @return The coordinate that represent the player location in game.
	 */
	ICoordinates getCoordinates();

	/**
	 * @return The sound sphere associated to this player. If another player is inside this sound sphere they will be able to hear
	 *         each other.
	 */
	ISoundSphere getSoundSphere();

	/**
	 * Adds an effect on the audio stream of the speaking player.
	 * 
	 * @param speaking The speaking player, ie the audio stream for which an effect shall be added.
	 * @param index    The index at which the effect shall be added. If the index is greater than the size of the list of effect
	 * @param effect   The effect to add.
	 */
	void addEffect(IVoxyPlayer speaking, int index, IVoxyEffect effect);

	/**
	 * Removes an effect, if registered, to apply on the audio stream of a player.
	 * 
	 * @param speaking   The speaking player, ie the audio stream for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffect(IVoxyPlayer speaking, String effectName);

	/**
	 * Updates the parameters of an effect.
	 * 
	 * @param speaking The speaking player, ie the audio stream for which an effect shall be updated.
	 * @param effect   The effect to update.
	 */
	public void updateEffect(IVoxyPlayer speaking, IVoxyEffect effect);
}
