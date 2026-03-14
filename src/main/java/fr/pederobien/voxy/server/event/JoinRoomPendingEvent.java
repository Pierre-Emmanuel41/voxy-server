package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class JoinRoomPendingEvent extends VoxyRoomEvent {
	private final IVoxyPlayer player;

	/**
	 * Creates an event thrown when a player joined the pending queue of a room.
	 * 
	 * @param room   The room a player is about to join.
	 * @param player The player that is about to join a room.
	 */
	public JoinRoomPendingEvent(IVoxyRoom room, IVoxyPlayer player) {
		super(room);

		this.player = player;
	}

	/**
	 * @return The player that is about to join a room.
	 */
	public IVoxyPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("room=" + getRoom().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
