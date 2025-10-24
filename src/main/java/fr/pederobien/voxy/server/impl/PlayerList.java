package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.JoinRoomPreEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPreEvent;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class PlayerList implements IPlayerList {
	private final VoxyRoom room;
	private final List<IVoxyPlayer> players;
	private final Object lock;

	public PlayerList(VoxyRoom room) {
		this.room = room;

		players = new ArrayList<IVoxyPlayer>();
		lock = new Object();
	}

	@Override
	public boolean add(IVoxyPlayer player) {
		synchronized (lock) {

			// Player's name shall be unique
			if (getByName(player.getName()) != null)
				return false;

			// Notifying first that a player is about to join a room, if event not cancelled then the player is added
			JoinRoomPreEvent preEvent = new JoinRoomPreEvent(room, player);
			Runnable exe = () -> {
				players.add(player);

				info("Player %s joined the room", player.getName());
				EventManager.callEvent(new JoinRoomPostEvent(room, player));
			};

			EventManager.callEvent(preEvent, exe);

			return preEvent.isCancelled();
		}
	}

	@Override
	public boolean remove(String name) {
		synchronized (lock) {

			// Player's name shall be unique
			IVoxyPlayer player = getByName(name);
			if (player == null)
				return false;

			// Notifying first that a player is about to leave, if event not cancelled then the player is removed
			LeaveRoomPreEvent preEvent = new LeaveRoomPreEvent(room, player);
			Runnable exe = () -> {

				// Removing the room from the rooms list
				players.remove(player);

				info("Player %s left the room", player.getName());
				EventManager.callEvent(new LeaveRoomPostEvent(room, player));
			};

			EventManager.callEvent(preEvent, exe);
			return preEvent.isCancelled();
		}
	}

	@Override
	public void removeAll() {
		List<String> names = new ArrayList<String>();
		players.forEach(player -> names.add(player.getName()));

		for (String name : names)
			remove(name);
	}

	@Override
	public Optional<IVoxyPlayer> get(String name) {
		IVoxyPlayer player;
		synchronized (lock) {
			player = getByName(name);
		}

		return Optional.ofNullable(player);
	}

	@Override
	public List<IVoxyPlayer> toList() {
		return Collections.unmodifiableList(players);
	}

	/**
	 * Get the player associated to the given name.
	 * 
	 * @param name The name of the player to get.
	 * 
	 * @return Null if no player is registered for the given name, the player otherwise.
	 */
	private IVoxyPlayer getByName(String name) {
		for (IVoxyPlayer player : players)
			if (player.getName().equals(name))
				return player;

		return null;
	}

	/**
	 * Creates a LogEvent with log level INFO and the given formatted text.
	 *
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	private void info(String format, Object... args) {
		Logger.info("%s - %s", room, String.format(format, args));
	}
}
