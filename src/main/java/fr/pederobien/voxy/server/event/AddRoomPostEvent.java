package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

import java.util.StringJoiner;

public class AddRoomPostEvent extends VoxyServerEvent {
    private final IVoxyRoom room;

    /**
     * Event thrown when a room has been added to the server.
     *
     * @param server The server on which the room has been added.
     * @param room   The added room.
     */
    public AddRoomPostEvent(IVoxyServer server, IVoxyRoom room) {
        super(server);

        this.room = room;
    }

    /**
     * @return The added room.
     */
    public IVoxyRoom getRoom() {
        return room;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("server=" + getServer());
        joiner.add("room=" + getRoom().getName());
        return String.format("%s_%s", getName(), joiner);
    }
}
