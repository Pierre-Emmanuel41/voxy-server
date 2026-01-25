package fr.pederobien.voxy.server.interfaces;

public interface IVoxyRoom {

	/***
	 * @return The server to which this room is associated.
	 */
	IVoxyServer getServer();

	/**
	 * @return The name of the room.
	 */
	String getName();

	/**
	 * Set the name of this room.
	 *
	 * @param name The new room's name.
	 * 
	 * @return True if the room has been renamed, false otherwise.
	 */
	boolean setName(String name);

	/**
	 * @return The map of player currently connected in this room. This map is unmodifiable.
	 */
	IPlayerList getPlayers();

	/**
	 * Enable or disable the play back. When player back is enabled, a player will here back the audio samples sent to the server.
	 * 
	 * @param playBack True to enable play back, false to disable.
	 */
	void setPlayBack(boolean playBack);
}
