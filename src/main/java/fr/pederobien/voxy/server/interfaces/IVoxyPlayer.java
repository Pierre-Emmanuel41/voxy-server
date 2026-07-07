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
	 * Set the effect to apply, on the listener side, on the audio stream of this player.
	 * 
	 * @param listener   The player that shall apply an effect on the audio stream of this player.
	 * @param effectName The name of the effect to apply.
	 * @param values     The effect parameters value.
	 */
	void setEffect(IVoxyPlayer listener, String effectName, Object... values);

	/**
	 * Removes the effect currently applied on the listener side.
	 * 
	 * @param listener The player that shall remove the effect on the audio stream of this player.
	 */
	void removeEffect(IVoxyPlayer listener);

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
