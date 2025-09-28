package fr.pederobien.voxy.server.event;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

import java.util.StringJoiner;

public class RemoveRoomPrevent extends VoxyServerEvent implements ICancellable {
    private final IVoxyRoom room;
    private boolean isCancelled;

    /**
     * Creates an event thrown when a room is about to be removed from the server.
     *
     * @param server The server on which a room is about to be removed.
     * @param room   The room that is about to be removed.
     */
    public RemoveRoomPrevent(IVoxyServer server, IVoxyRoom room) {
        super(server);

        this.room = room;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    /**
     * @return The room that is about to be removed.
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
