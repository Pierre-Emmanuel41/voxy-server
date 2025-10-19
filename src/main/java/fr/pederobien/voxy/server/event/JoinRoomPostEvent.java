package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class JoinRoomPostEvent extends VoxyRoomEvent {
	private final IVoxyPlayer player;

	/**
	 * Creates an event when a player has joined a room.
	 * 
	 * @param room   The room the player joined.
	 * @param player The player that joined the room.
	 */
	public JoinRoomPostEvent(IVoxyRoom room, IVoxyPlayer player) {
		super(room);

		this.player = player;
	}

	/**
	 * @return The player that joined a room.
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
