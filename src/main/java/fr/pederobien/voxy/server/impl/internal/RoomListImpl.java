package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.impl.RoomList;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class RoomListImpl extends ServerElement {
	private final List<VoxyRoomImpl> rooms;
	private final Object lock;

	private final IRoomList external;

	/**
	 * Creates the implementation of a rooms list.
	 * 
	 * @param server The server implementation associated to this rooms list implementation.
	 */
	protected RoomListImpl(VoxyServerImpl server) {
		super(server);

		rooms = new ArrayList<VoxyRoomImpl>();
		lock = new Object();
		external = new RoomList(this);
	}

	/**
	 * Adds the room to this list and throws an AddRoomPostEvent to notify each client.
	 * 
	 * @param name The name of the room to create.
	 */
	public void add(String name) {
		VoxyRoomImpl roomImpl = new VoxyRoomImpl(getServer(), name);
		synchronized (lock) {
			rooms.add(roomImpl);
		}

		info("Room %s has been added", name);

		roomImpl.initialize();
		EventManager.callEvent(new AddRoomPostEvent(getServer().getExternal(), roomImpl.getExternal()));
	}

	/**
	 * Removes the room from this list and throws a RemoveRoomPostEvent to notify each client.
	 * 
	 * @param name The name of the room to create.
	 */
	public void remove(VoxyRoomImpl roomImpl) {
		boolean removed;
		synchronized (lock) {
			removed = rooms.remove(roomImpl);
		}

		if (!removed)
			return;

		info("Room %s has been removed", roomImpl.getName());
		EventManager.callEvent(new RemoveRoomPostEvent(getServer().getExternal(), roomImpl.getExternal()));
	}

	/**
	 * Get the room associated to the given name.
	 * 
	 * @param name The name of the room to get.
	 * 
	 * @return Null if no room is registered for the given name, the room otherwise.
	 */
	public VoxyRoomImpl getByName(String name) {
		synchronized (lock) {
			for (VoxyRoomImpl room : rooms)
				if (room.getName().equals(name))
					return room;
		}

		return null;
	}

	/**
	 * Get the room in which the player associated to the given name is.
	 * 
	 * @param name The name of the player.
	 * 
	 * @return The room in which the player is, null if the player is not registered in a room.
	 */
	public VoxyRoomImpl getRoomByPlayerName(String name) {
		synchronized (lock) {
			for (VoxyRoomImpl room : rooms)
				if (room.getPlayers().getByName(name) != null)
					return room;
		}

		return null;
	}

	/**
	 * @return The number of rooms in the underlying list.
	 */
	public int size() {
		return rooms.size();
	}

	/**
	 * @return The list of rooms to use externally.
	 */
	public List<IVoxyRoom> toList() {
		List<IVoxyRoom> list = new ArrayList<IVoxyRoom>();
		synchronized (lock) {
			for (VoxyRoomImpl roomImpl : rooms)
				list.add(roomImpl.getExternal());
		}

		return list;
	}

	/**
	 * Performs the given action over each rooms in this list.
	 * 
	 * @param action The action to perform.
	 */
	public void foreach(Consumer<VoxyRoomImpl> action) {
		synchronized (lock) {
			rooms.forEach(action);
		}
	}

	/**
	 * @return The rooms list to use externally.
	 */
	public IRoomList getExternal() {
		return external;
	}
}
