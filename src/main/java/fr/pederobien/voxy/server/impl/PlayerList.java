package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.JoinRoomPreEvent;
import fr.pederobien.voxy.server.impl.internal.PlayerListImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class PlayerList implements IPlayerList {
	private final PlayerListImpl listImpl;

	/**
	 * Creates a players list.
	 * 
	 * @param listImpl The implementation of the players list.
	 */
	public PlayerList(PlayerListImpl listImpl) {
		this.listImpl = listImpl;
	}

	@Override
	public boolean add(String name) {

		// A player is already registered
		if (listImpl.getByName(name) != null)
			return false;

		VoxyPlayerImpl playerImpl = listImpl.getServer().getPlayerByName(name);

		// The player does not exist
		if (playerImpl == null)
			return false;

		// Notifying first that a player is about to join a room, if event not cancelled then the player is added
		JoinRoomPreEvent preEvent = new JoinRoomPreEvent(listImpl.getRoomImpl().getExternal(), playerImpl.getExternal());
		EventManager.callEvent(preEvent, () -> listImpl.add(playerImpl));

		// Event not cancelled so player added
		return !preEvent.isCancelled();
	}

	@Override
	public boolean remove(String name) {

		// No player registered
		if (listImpl.getByName(name) == null)
			return false;

		VoxyPlayerImpl playerImpl = listImpl.getServer().getPlayerByName(name);

		// The player does not exist
		if (playerImpl == null)
			return false;

		listImpl.remove(playerImpl);
		return true;
	}

	@Override
	public void removeAll() {
		listImpl.removeAll();
	}

	@Override
	public Optional<IVoxyPlayer> get(String name) {
		VoxyPlayerImpl playerImpl = listImpl.getByName(name);
		return playerImpl == null ? Optional.empty() : Optional.of(playerImpl.getExternal());
	}

	@Override
	public List<IVoxyPlayer> toList() {
		return listImpl.toList();
	}
}
