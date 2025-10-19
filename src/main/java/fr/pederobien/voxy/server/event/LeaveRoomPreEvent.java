package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class LeaveRoomPreEvent extends VoxyRoomEvent implements ICancellable {
	private final IVoxyPlayer player;
	private boolean isCancelled;

	/**
	 * Creates an event when a player is about to leave a server.
	 * 
	 * @param room   The room that the player is about to leave.
	 * @param player The player that is about to leave a room.
	 */
	public LeaveRoomPreEvent(IVoxyRoom room, IVoxyPlayer player) {
		super(room);

		this.player = player;
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
	 * @return The player that is about to leave a room.
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
