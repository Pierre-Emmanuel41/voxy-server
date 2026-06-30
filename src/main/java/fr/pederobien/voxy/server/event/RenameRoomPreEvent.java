package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class RenameRoomPreEvent extends VoxyRoomEvent implements ICancellable {
	private final String newName;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event raised when a room is about to be renamed.
	 *
	 * @param room    The room that is about to be renamed.
	 * @param newName The new room's name.
	 * @param source  The source that requires to rename a room.
	 */
	public RenameRoomPreEvent(IVoxyRoom room, String newName, ISource source) {
		super(room);

		this.newName = newName;
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
	 * @return The new room's name.
	 */
	public String getNewName() {
		return newName;
	}

	/**
	 * @return The source that requires to rename a room.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("currentName=" + getRoom().getName());
		joiner.add("newName=" + getNewName());
		joiner.add("source=" + getSource().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
