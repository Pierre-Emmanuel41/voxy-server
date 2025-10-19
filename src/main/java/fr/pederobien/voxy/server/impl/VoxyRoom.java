package fr.pederobien.voxy.server.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.JoinRoomPreEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPreEvent;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPrevent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyRoom implements IVoxyRoom, IEventListener {
	private final VoxyServer server;
	private final VocalServer vocalServer;
	private final Map<String, IVoxyPlayer> players;
	private String name;
	private Object lock;

	/***
	 * Creates a room where players can speak together.
	 *
	 * @param server The server on which this room is created.
	 * @param name   The room name.
	 */
	public VoxyRoom(VoxyServer server, String name) {
		this.server = server;
		this.name = name;

		players = new HashMap<String, IVoxyPlayer>();
		vocalServer = new VocalServer(this);
		vocalServer.open();

		lock = new Object();

		EventManager.registerListener(this);
	}

	@Override
	public IVoxyServer getServer() {
		return server;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		RenameRoomPrevent preEvent = new RenameRoomPrevent(this, name);
		RenameRoomPostEvent postEvent = new RenameRoomPostEvent(this, this.name);
		Runnable exe = () -> {
			info("Room %s has been renamed as %s", this.name, name);
			this.name = name;
		};

		EventManager.callEvent(preEvent, exe, postEvent);
	}

	@Override
	public Map<String, IVoxyPlayer> getPlayers() {
		return Collections.unmodifiableMap(players);
	}

	@Override
	public void add(IVoxyPlayer player) {
		synchronized (lock) {
			if (players.get(player.getName()) != null)
				return;
		}

		JoinRoomPreEvent preEvent = new JoinRoomPreEvent(this, player);
		JoinRoomPostEvent postEvent = new JoinRoomPostEvent(this, player);
		Runnable exe = () -> {
			info("Player %s joined room %s", player.getName(), name);
			synchronized (lock) {
				players.put(player.getName(), player);
			}
		};

		EventManager.callEvent(preEvent, exe, postEvent);
	}

	@Override
	public void remove(String name) {
		IVoxyPlayer player;
		synchronized (lock) {
			if ((player = players.get(name)) == null)
				return;
		}

		LeaveRoomPreEvent preEvent = new LeaveRoomPreEvent(this, player);
		LeaveRoomPostEvent postEvent = new LeaveRoomPostEvent(this, player);
		Runnable exe = () -> {
			info("Player %s left room %s", player.getName(), name);
			synchronized (lock) {
				players.remove(player.getName());
			}
		};

		EventManager.callEvent(preEvent, exe, postEvent);
	}

	@Override
	public int getPort() {
		return vocalServer.getPort();
	}

	@Override
	public String toString() {
		return name;
	}

	@EventHandler
	private void onRoomRemoved(RemoveRoomPostEvent event) {
		if (event.getRoom() != this)
			return;

		vocalServer.close();
		vocalServer.dispose();
	}

	/**
	 * Creates a LogEvent with log level INFO and the given formatted text.
	 *
	 * @param format The formatter if the message to display has arguments.
	 * @param args   The arguments of the message to display.
	 */
	private void info(String format, Object... args) {
		Logger.info("%s %s", getServer(), String.format(format, args));
	}
}
