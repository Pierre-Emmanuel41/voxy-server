package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

import java.util.StringJoiner;

public class RemoveRoomPostEvent extends VoxyServerEvent {
    private final IVoxyRoom room;

    /**
     * Event thrown when a room has been removed from the server.
     *
     * @param server The server on which the room has been removed.
     * @param room   The removed room.
     */
    public RemoveRoomPostEvent(IVoxyServer server, IVoxyRoom room) {
        super(server);

        this.room = room;
    }

    /**
     * @return The removed room.
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
