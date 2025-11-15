package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.impl.PlayerList;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class PlayerListImpl extends ServerElement {
	private final VoxyRoomImpl roomImpl;
	private final List<VoxyPlayerImpl> players;
	private final Object lock;

	private final IPlayerList external;

	/**
	 * Creates the implementation of a list of players associated to a room.
	 * 
	 * @param roomImpl The voxy room implementation associated to this players list implementation.
	 */
	protected PlayerListImpl(VoxyRoomImpl roomImpl) {
		super(roomImpl.getServer());

		this.roomImpl = roomImpl;

		players = new ArrayList<VoxyPlayerImpl>();
		lock = new Object();
		external = new PlayerList(this);
	}

	/**
	 * Adds the player to the list and throws a JoinRoomPostEvent to notify each client.
	 * 
	 * @param playerImpl The player implementation to add to the list.
	 */
	public void add(VoxyPlayerImpl playerImpl) {
		synchronized (lock) {
			players.add(playerImpl);
		}

		info("Player %s joined the room %s", playerImpl, roomImpl);
		EventManager.callEvent(new JoinRoomPostEvent(roomImpl.getExternal(), playerImpl.getExternal()));
	}

	/**
	 * Removes the player from the list and throws a LeaveRoomPostEvent to notify each client.
	 * 
	 * @param name The name of the player to remove.
	 */
	public void remove(VoxyPlayerImpl playerImpl) {
		boolean removed;
		synchronized (lock) {
			removed = players.remove(playerImpl);
		}

		if (!removed)
			return;

		info("Player %s left the room %s", playerImpl, roomImpl);
		EventManager.callEvent(new LeaveRoomPostEvent(roomImpl.getExternal(), playerImpl.getExternal()));
	}

	/**
	 * Removes each player registered in this list.
	 */
	public void removeAll() {
		List<VoxyPlayerImpl> copy = new ArrayList<VoxyPlayerImpl>(players);

		synchronized (lock) {
			players.clear();

			for (VoxyPlayerImpl playerImpl : copy)
				EventManager.callEvent(new LeaveRoomPostEvent(roomImpl.getExternal(), playerImpl.getExternal()));
		}
	}

	/**
	 * Get the player associated to the given name.
	 * 
	 * @param name The name of the player to get.
	 * 
	 * @return Null if no player is registered for the given name, the player otherwise.
	 */
	public VoxyPlayerImpl getByName(String name) {
		synchronized (lock) {
			for (VoxyPlayerImpl player : players)
				if (player.getName().equals(name))
					return player;
		}

		return null;
	}

	/**
	 * @return The list of players to use externally.
	 */
	public List<IVoxyPlayer> toList() {
		List<IVoxyPlayer> list = new ArrayList<IVoxyPlayer>();
		synchronized (lock) {
			for (VoxyPlayerImpl playerImpl : players)
				list.add(playerImpl.getExternal());
		}

		return list;
	}

	/**
	 * Performs the given action over each players in this list.
	 * 
	 * @param action The action to perform.
	 */
	public void foreach(Consumer<VoxyPlayerImpl> action) {
		synchronized (lock) {
			players.forEach(action);
		}
	}

	/**
	 * @return The implementation of the room associated to this players list.
	 */
	public VoxyRoomImpl getRoomImpl() {
		return roomImpl;
	}

	/**
	 * @return The players list to be used externally.
	 */
	public IPlayerList getExternal() {
		return external;
	}
}
