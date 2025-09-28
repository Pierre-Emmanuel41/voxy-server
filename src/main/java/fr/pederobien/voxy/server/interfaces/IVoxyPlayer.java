package fr.pederobien.voxy.server.interfaces;

public interface IVoxyPlayer {

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
	 * Set if this player is mute.
	 *
	 * @param isMute True if this player is mute, false otherwise.
	 */
	void setMute(boolean isMute);

	/**
	 * @return True if the player disabled it speakers, false otherwise.
	 */
	boolean isDeaf();
}
