package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

import java.util.StringJoiner;

public class RenameRoomPostEvent extends VoxyRoomEvent {
    private final String oldName;
    private boolean isCancelled;

    /**
     * Creates an event raised when a room has been renamed.
     *
     * @param room    The room that is has been renamed.
     * @param oldName the old room's name.
     */
    public RenameRoomPostEvent(IVoxyRoom room, String oldName) {
        super(room);

        this.oldName = oldName;
    }

    /**
     * @return The old room's name.
     */
    public String getOldName() {
        return oldName;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        joiner.add("currentName =" + getRoom().getName());
        joiner.add("oldName=" + getOldName());
        return String.format("%s_%s", getName(), joiner);
    }
}
