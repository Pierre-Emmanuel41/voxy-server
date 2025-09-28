package fr.pederobien.voxy.server.interfaces;

import java.util.Map;

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
    Map<String, IVoxyPlayer> getPlayers();

    /**
     * Adds the given player to this room.
     *
     * @param player The player to add.
     */
    void add(IVoxyPlayer player);

    /**
     * Removes the given player from this room.
     *
     * @param player The player to remove.
     */
    void remove(IVoxyPlayer player);

    /**
     * @return The UDP port used by the players to communicate.
     */
    int getPort();
}
