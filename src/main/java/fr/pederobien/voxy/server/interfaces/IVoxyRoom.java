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
	 */
	void setName(String name);

	/**
	 * @return The map of player currently connected in this room. This map is unmodifiable.
	 */
	IPlayerList getPlayers();

	/**
	 * @return The UDP port used by the players to communicate.
	 */
	int getPort();
}
