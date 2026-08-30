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
	 * Set the effect to apply on the audio stream of the speaking player.
	 * 
	 * @param speaker    The player that is speaking.
	 * @param effectName The name of the effect to apply on the audio stream of the speaking player.
	 * @param holder     A holder that contains the effect name and gather effect parameter's name / parameter's value.
	 */
	void addEffect(IVoxyPlayer speaker, int index, IEffect holder);

	/**
	 * Stops the effect associated to the given effectName. The effect will transition smoothly from applied to not applied. Once
	 * stopped completely, the effect will be removed.
	 * 
	 * @param name       The name of the audio stream for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	void removeEffect(IVoxyPlayer speaker, String effectName);

	/**
	 * Update the parameters of an effect. The parameters defines how the effect modifies the audio stream.
	 * 
	 * @param name   The name of the audio stream on which an effect shall be modified.
	 * @param holder A holder that contains the effect name and gather effect parameter's name / parameter's value.
	 */
	void updateEffect(IVoxyPlayer speaker, IEffect holder);

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
}
