package fr.pederobien.voxy.server.event;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

import java.util.StringJoiner;

public class RenameRoomPrevent extends VoxyRoomEvent implements ICancellable {
    private final String newName;
    private boolean isCancelled;

    /**
     * Creates an event raised when a room is about to be renamed.
     *
     * @param room    The room that is about to be renamed.
     * @param newName the new room's name.
     */
    public RenameRoomPrevent(IVoxyRoom room, String newName) {
        super(room);

        this.newName = newName;
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
     * @return The new room's name.
     */
    public String getNewName() {
        return newName;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("currentName =" + getRoom().getName());
        joiner.add("newName=" + getNewName());
        return String.format("%s_%s", getName(), joiner);
    }
}
