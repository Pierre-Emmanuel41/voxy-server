package fr.pederobien.voxy.server.interfaces;

import java.util.Map;

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

    /***
     * @return The map of players currently connected on this server. This map is unmodifiable.
     */
    Map<String, IVoxyPlayer> getPlayers();

    /**
     * @return The map of rooms where player can talk to each other. This map is unmodifiable.
     */
    Map<String, IVoxyRoom> getRooms();

    /**
     * Creates a room with the given name if it not yet already exists.
     *
     * @param name The room's name.
     */
    void add(String name);

    /**
     * Removes the room associated to the given name.
     *
     * @param name The name of the room to remove.
     */
    void remove(String name);
}
