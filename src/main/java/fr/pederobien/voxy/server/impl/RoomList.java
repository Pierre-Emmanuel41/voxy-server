package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.impl.internal.RoomListImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class RoomList implements IRoomList {
	private final RoomListImpl listImpl;

	/**
	 * Creates a list of rooms associated to the given server.
	 * 
	 * @param listImpl The implementation of this rooms list.
	 */
	public RoomList(RoomListImpl listImpl) {
		this.listImpl = listImpl;
	}

	@Override
	public boolean add(String name) {

		// A room is already registered for the given name
		if (listImpl.getByName(name) != null)
			return false;

		AddRoomPreEvent preEvent = new AddRoomPreEvent(listImpl.getServer().getExternal(), name);
		EventManager.callEvent(preEvent, () -> listImpl.add(name));

		// Event not cancelled, a room has been created and added
		return !preEvent.isCancelled();
	}

	@Override
	public boolean remove(String name) {

		// The room does not exist
		VoxyRoomImpl roomImpl = listImpl.getByName(name);
		if (listImpl.getByName(name) == null)
			return false;

		RemoveRoomPrevent preEvent = new RemoveRoomPrevent(listImpl.getServer().getExternal(), roomImpl.getExternal());
		EventManager.callEvent(preEvent, () -> listImpl.remove(roomImpl));

		// Event not cancelled, the room has been removed.
		return !preEvent.isCancelled();
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		VoxyRoomImpl roomImpl = listImpl.getByName(name);
		return roomImpl == null ? Optional.empty() : Optional.of(roomImpl.getExternal());
	}

	@Override
	public List<IVoxyRoom> toList() {
		return listImpl.toList();
	}
}
