package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class RemoveRoomPreEvent extends VoxyServerEvent implements ICancellable {
	private final IVoxyRoom room;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a room is about to be removed from the server.
	 *
	 * @param server The server on which a room is about to be removed.
	 * @param room   The room that is about to be removed.
	 * @param source The source that requires to remove a room from the server.
	 */
	public RemoveRoomPreEvent(IVoxyServer server, IVoxyRoom room, ISource source) {
		super(server);

		this.room = room;
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
	 * @return The room that is about to be removed.
	 */
	public IVoxyRoom getRoom() {
		return room;
	}

	/**
	 * @return The source that requires to remove a room from the server.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		joiner.add("room=" + getRoom().getName());
		joiner.add("source=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
