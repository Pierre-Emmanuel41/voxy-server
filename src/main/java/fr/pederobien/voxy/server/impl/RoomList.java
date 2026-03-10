package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.impl.internal.RoomListImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.server.interfaces.IRoomList;
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
	public boolean add(String name) {

		// A room is already registered for the given name
		if (impl.getByName(name) != null)
			return false;

		debug("Adding room %s", name);

		AddRoomPreEvent preEvent = new AddRoomPreEvent(impl.getServer().getExternal(), name);
		EventManager.callEvent(preEvent, () -> impl.add(name));

		// Event not cancelled, a room has been created and added
		return !preEvent.isCancelled();
	}

	@Override
	public boolean remove(String name) {

		// The room does not exist
		VoxyRoomImpl roomImpl = impl.getByName(name);
		if (impl.getByName(name) == null)
			return false;

		debug("Removing room %s", name);

		RemoveRoomPrevent preEvent = new RemoveRoomPrevent(impl.getServer().getExternal(), roomImpl.getExternal());
		EventManager.callEvent(preEvent, () -> impl.remove(roomImpl));

		// Event not cancelled, the room has been removed.
		return !preEvent.isCancelled();
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		VoxyRoomImpl room = impl.getByName(name);
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
