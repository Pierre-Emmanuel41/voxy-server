package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.AddRoomPostEvent;
import fr.pederobien.voxy.server.event.AddRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPrevent;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class RoomList implements IRoomList {
	private final VoxyServer server;
	private final List<IVoxyRoom> rooms;
	private final Object lock;

	/**
	 * Creates a list of rooms associated to the given server.
	 * 
	 * @param server The server associated to this list.
	 */
	public RoomList(VoxyServer server) {
		this.server = server;

		rooms = new ArrayList<IVoxyRoom>();
		lock = new Object();
	}

	@Override
	public boolean add(String name) {
		synchronized (lock) {

			// Room's name shall be unique
			if (getByName(name) != null)
				return false;

			// Notifying first that a room is about to be added, if event not cancelled then the room is added
			AddRoomPreEvent preEvent = new AddRoomPreEvent(server, name);
			Runnable exe = () -> {
				IVoxyRoom room = new VoxyRoom(server, name);
				rooms.add(room);

				info("Room %s has been added", room.getName());
				EventManager.callEvent(new AddRoomPostEvent(server, room));
			};

			EventManager.callEvent(preEvent, exe);
			return preEvent.isCancelled();
		}
	}

	@Override
	public boolean remove(String name) {
		synchronized (lock) {

			// Room's name shall be unique
			IVoxyRoom room = getByName(name);
			if (room == null)
				return false;

			// Notifying first that a room is about to be removed, if event not cancelled then the room is removed
			RemoveRoomPrevent preEvent = new RemoveRoomPrevent(server, room);
			Runnable exe = () -> {

				// Removing each player from this room
				room.getPlayers().removeAll();

				// Removing the room from the rooms list
				rooms.remove(room);

				info("Room %s has been removed", room.getName());
				EventManager.callEvent(new RemoveRoomPostEvent(server, room));
			};

			EventManager.callEvent(preEvent, exe);
			return preEvent.isCancelled();
		}
	}

	@Override
	public Optional<IVoxyRoom> get(String name) {
		IVoxyRoom room;
		synchronized (name) {
			room = getByName(name);
		}

		return Optional.ofNullable(room);
	}

	@Override
	public List<IVoxyRoom> toList() {
		return Collections.unmodifiableList(rooms);
	}

	/**
	 * Get the room associated to the given name.
	 * 
	 * @param name The name of the room to get.
	 * 
	 * @return Null if no room is registered for the given name, the room otherwise.
	 */
	private IVoxyRoom getByName(String name) {
		for (IVoxyRoom room : rooms)
			if (room.getName().equals(name))
				return room;

		return null;
	}

	/**
	 * Creates a LogEvent with log level INFO and the given formatted text.
	 *
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	private void info(String format, Object... args) {
		Logger.info("%s %s", server, String.format(format, args));
	}
}
