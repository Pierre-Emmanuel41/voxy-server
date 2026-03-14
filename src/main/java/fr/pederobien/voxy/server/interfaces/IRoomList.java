package fr.pederobien.voxy.server.interfaces;

import java.util.List;
import java.util.Optional;

public interface IRoomList {

	/**
	 * Check if there is a room registered for the given name. If a room exists, then returns false. If no room exists for the given
	 * name, a AddRoomPreEvent is thrown. If the event is cancelled, then returns false. If the AddRoomPreEvent is not cancelled, then
	 * a room with the given name is created and a AddRoomPostEvent is thrown to notify each client.
	 * 
	 * @param name The name of the room to create.
	 * 
	 * @return True if a room has been added, false otherwise.
	 */
	boolean add(String name);

	/**
	 * Check if there is a room registered for the given name. If no room exists, then returns false. If a room exists for the given
	 * name, a RemoveRoomPreEvent is thrown. If the event is cancelled, then returns false. If the RemoveRoomPreEvent is not
	 * cancelled, then the room is removed and a RemoveRoomPostEvent is thrown to notify each client.
	 * 
	 * @param name The name of the room to remove.
	 * 
	 * @return True if the room has been removed, false otherwise.
	 */
	boolean remove(String name);

	/**
	 * Finds the room associated to the given name.
	 * 
	 * @param name The room's name to find.
	 * 
	 * @return An optional containing the room with the given name if it exists, an empty optional otherwise.
	 */
	Optional<IVoxyRoom> get(String name);

	/**
	 * Get the room in which the player associated to the given name is.
	 * 
	 * @param name The name of the player.
	 * 
	 * @return An optional containing the room in which the player is, an empty optional if the player is not registered in a room.
	 */
	Optional<IVoxyRoom> getRoomByPlayerName(String name);

	/**
	 * @return The number of rooms in the underlying list.
	 */
	int size();

	/**
	 * @return An unmodifiable list containing the rooms registered in this list.
	 */
	List<IVoxyRoom> toList();
}
