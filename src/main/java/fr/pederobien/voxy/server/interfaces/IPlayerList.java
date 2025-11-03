package fr.pederobien.voxy.server.interfaces;

import java.util.List;
import java.util.Optional;

public interface IPlayerList {

	/**
	 * Check if there is a player registered for the given name. If a player is registered, then returns false. If no player is
	 * registered, a JoinRoomPreEvent is thrown. If the event is cancelled, then returns false. If the JoinRoomPreEvent is not
	 * cancelled, then the player is added to the list and a JoinRoomPostEvent is thrown to notify each client.
	 * 
	 * @param name The name of the player to add to the list.
	 * 
	 * @return True if the player has been added, false otherwise.
	 */
	boolean add(String name);

	/**
	 * Check if there is a player registered for the given name. If no player is registered, then returns false. If a player is
	 * registered for the given name, a LeaveRoomPostEvent is thrown to notify each client.
	 * 
	 * @param name The name of the player to remove.
	 * 
	 * @return True if the player has been removed, false otherwise.
	 */
	boolean remove(String name);

	/**
	 * Removes each player registered in this list.
	 */
	void removeAll();

	/**
	 * Finds the player associated to the given name.
	 * 
	 * @param name The player's name to find.
	 * 
	 * @return An optional containing the player with the given name if it exists, an empty optional otherwise.
	 */
	Optional<IVoxyPlayer> get(String name);

	/**
	 * @return A list containing the players registered in this list.
	 */
	List<IVoxyPlayer> toList();
}
