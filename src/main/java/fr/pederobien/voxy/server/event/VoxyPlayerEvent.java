package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerEvent extends VoxyEvent {
    private final IVoxyPlayer player;

    /**
     * Creates a voxy player event.
     *
     * @param player The player involved in this event.
     */
    public VoxyPlayerEvent(IVoxyPlayer player) {
        this.player = player;
    }

    /**
     * @return The player involved in this event.
     */
    public IVoxyPlayer getPlayer() {
        return player;
    }
}
