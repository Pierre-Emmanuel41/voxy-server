package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyRoomEvent extends VoxyEvent {
    private final IVoxyRoom room;

    /**
     * Creates a voxy room event.
     *
     * @param room The room involved in this event.
     */
    public VoxyRoomEvent(IVoxyRoom room) {
        this.room = room;
    }

    /**
     * @return The room involved in this event.
     */
    public IVoxyRoom getRoom() {
        return room;
    }
}
