package fr.pederobien.voxy.server.interfaces;

public interface IVoxyServer {

	/**
	 * @return The vocal server name.
	 */
	String getName();

	/**
	 * Open the server to let the clients connect.
	 */
	void open();

	/**
	 * Close the server, each player currently connected will be kicked.
	 */
	void close();

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 */
	void dispose();

	/**
	 * @return The list of rooms associated to this server.
	 */
	IRoomList getRooms();
}
