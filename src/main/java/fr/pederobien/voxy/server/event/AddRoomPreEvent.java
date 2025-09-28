package fr.pederobien.voxy.server.event;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

import java.util.StringJoiner;

public class AddRoomPreEvent extends VoxyServerEvent implements ICancellable {
    private final String name;
    private boolean isCancelled;

    /**
     * Event thrown when a room is about to be added to the server.
     *
     * @param server The server on which the room is about to be added.
     * @param name   The room's name to add.
     */
    public AddRoomPreEvent(IVoxyServer server, String name) {
        super(server);

        this.name = name;
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
     * @return The name of the room that is about to be added.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("server=" + getServer());
        joiner.add("name=" + getName());
        return String.format("%s_%s", getName(), joiner);
    }
}
