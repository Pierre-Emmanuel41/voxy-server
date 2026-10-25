package fr.pederobien.voxy.server.interfaces;

import java.util.stream.Stream;

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
	 * @param name   The new room's name.
	 * @param source The source that requires to rename a room.
	 * 
	 * @return True if the room has been renamed, false otherwise.
	 */
	boolean setName(String name, ISource source);

	/**
	 * @return The map of player currently connected in this room. This map is unmodifiable.
	 */
	IPlayerList getPlayers();

	/**
	 * Get a stream that contains the players registered in this room except the given player.
	 * 
	 * @param player The player to ignore.
	 * @return A stream that contains all the players registered in this room except the given player.
	 */
	default Stream<IVoxyPlayer> getPlayersExcept(IVoxyPlayer player) {
		return getPlayers().toList().stream().filter(other -> other != player);
	}
}
