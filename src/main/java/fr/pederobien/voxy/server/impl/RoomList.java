package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.impl.internal.RoomListImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class RoomList implements IRoomList {
	private final RoomListImpl impl;

	/**
	 * Creates a list of rooms associated to the given server.
	 * 
	 * @param impl The implementation of this rooms list.
	 */
	public RoomList(RoomListImpl impl) {
		this.impl = impl;
	}

	@Override
	public boolean add(String name, int port, ISource source) {

		// A room is already registered for the given name
		if (impl.getByName(name) != null)
			return false;

		debug("Adding room %s", name);

		// Event not cancelled, a room has been created and added
		return !impl.raiseAddRoomPreEvent(name, port, source, isCancelled -> {
			if (!isCancelled)
				impl.add(name, port);
		});
	}

	@Override
	public boolean remove(String name, ISource source) {

		// The room does not exist
		VoxyRoomImpl roomImpl = impl.getByName(name);
		if (impl.getByName(name) == null)
			return false;

		debug("Removing room %s", name);

		// Event not cancelled, the room has been removed.
		return !impl.raiseRemoveRoomPreEvent(roomImpl.getExternal(), source, isCancelled -> {
			if (!isCancelled)
				impl.remove(roomImpl);
		});
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		VoxyRoomImpl room = impl.getByName(name);
		return Optional.ofNullable(room == null ? null : room.getExternal());
	}

	@Override
	public Optional<IVoxyRoom> getRoomByPlayerName(String name) {
		VoxyRoomImpl room = impl.getRoomByPlayerName(name);
		return Optional.ofNullable(room == null ? null : room.getExternal());
	}

	@Override
	public int size() {
		return impl.size();
	}

	@Override
	public List<IVoxyRoom> toList() {
		return impl.toList();
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s - %s", impl.getServer(), String.format(format, args));
	}
}
