package fr.pederobien.voxy.server.interfaces;

import java.util.List;
import java.util.Optional;

public interface IVoxyServer {

	/**
	 * @return The vocal server name.
	 */
	String getName();

	/**
	 * Open the server to let the clients connect.
	 * 
	 * @return True if the server is successfully opened, false otherwise.
	 */
	boolean open();

	/**
	 * Close the server, each player currently connected will be kicked.
	 * 
	 * @return True if the server is successfully closed, false otherwise.
	 */
	boolean close();

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 * 
	 * @return True if the server is successfully disposed, false otherwise.
	 */
	boolean dispose();

	/**
	 * @return True if the server is opened, false otherwise.
	 */
	boolean isOpened();

	/**
	 * @return True if this server is disposed, false otherwise.
	 */
	boolean isDisposed();

	/**
	 * @return The list of rooms associated to this server.
	 */
	IRoomList getRooms();

	/**
	 * @return A copy of the players connected to the server.
	 */
	List<IVoxyPlayer> getPlayers();

	/**
	 * Get a player by its name.
	 * 
	 * @param name The name of the player to retrieve
	 * @return An optional containing the player associated to the given name if registered, an empty optional otherwise.
	 */
	Optional<IVoxyPlayer> getPlayerByName(String name);
}
