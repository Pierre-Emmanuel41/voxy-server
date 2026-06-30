package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class JoinRoomPreEvent extends VoxyRoomEvent implements ICancellable {
	private final IVoxyPlayer player;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event when a player is about to join a server.
	 * 
	 * @param room   The room that the player is about to join.
	 * @param player The player that is about to join a room.
	 * @param source The source that requires a player to join a room.
	 */
	public JoinRoomPreEvent(IVoxyRoom room, IVoxyPlayer player, ISource source) {
		super(room);

		this.player = player;
		this.source = source;
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
	 * @return The player that is about to join a room.
	 */
	public IVoxyPlayer getPlayer() {
		return player;
	}

	/**
	 * @param source The source that requires a player to join a room.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("room=" + getRoom().getName());
		joiner.add("player=" + getPlayer().getName());
		joiner.add("source=" + getSource());
		return String.format("%s_%s", getName(), joiner);
	}
}
