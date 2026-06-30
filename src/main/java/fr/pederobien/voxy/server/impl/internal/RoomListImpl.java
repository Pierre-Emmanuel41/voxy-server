package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPreEvent;
import fr.pederobien.voxy.server.impl.RoomList;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.ISource;
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
	 * Throws a AddRoomPreEvent associated to the given input parameters.
	 * 
	 * @param name     The name of the room to add.
	 * @param port     The port number to use for the vocal server.
	 * @param source   The source that requires to add a room on the server.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raiseAddRoomPreEvent(String name, int port, ISource source, Consumer<Boolean> callback) {
		AddRoomPreEvent event = new AddRoomPreEvent(getServer().getExternal(), name, port, source);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Adds the room to this list and throws an AddRoomPostEvent to notify each client.
	 * 
	 * @param name The name of the room to create.
	 * @param port The port number to use for the vocal server.
	 */
	public void add(String name, int port) {
		VoxyRoomImpl roomImpl = new VoxyRoomImpl(getServer(), name, port);
		synchronized (lock) {
			rooms.add(roomImpl);
		}

		info("Room %s has been added", name);

		roomImpl.initialize();
		EventManager.callEvent(new AddRoomPostEvent(getServer().getExternal(), roomImpl.getExternal()));
	}

	/**
	 * Throws a RemoveRoomPreEvent associated to the given input parameters.
	 * 
	 * @param room     The room to remove.
	 * @param source   The source that requires to remove a room from the server.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raiseRemoveRoomPreEvent(IVoxyRoom room, ISource source, Consumer<Boolean> callback) {
		RemoveRoomPreEvent event = new RemoveRoomPreEvent(getServer().getExternal(), room, source);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Removes the room from this list and throws a RemoveRoomPostEvent to notify each client.
	 * 
	 * @param room The implementation of the room to remove.
	 */
	public void remove(VoxyRoomImpl room) {
		boolean removed;
		synchronized (lock) {
			removed = rooms.remove(room);
		}

		if (!removed)
			return;

		room.getPlayers().removeAll();
		info("Room %s has been removed", room.getName());
		EventManager.callEvent(new RemoveRoomPostEvent(getServer().getExternal(), room.getExternal()));
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
