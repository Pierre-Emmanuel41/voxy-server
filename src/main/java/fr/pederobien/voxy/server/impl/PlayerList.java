package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.voxy.server.impl.internal.PlayerListImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class PlayerList implements IPlayerList {
	private final PlayerListImpl impl;

	/**
	 * Creates a players list.
	 * 
	 * @param impl The implementation of the players list.
	 */
	public PlayerList(PlayerListImpl impl) {
		this.impl = impl;
	}

	@Override
	public boolean add(String name, ISource source) {

		// A player is already registered
		if (impl.getByName(name) != null)
			return false;

		VoxyPlayerImpl player = impl.getServer().getPlayerByName(name);

		// The player does not exist
		if (player == null)
			return false;

		// Event not cancelled so player added
		return !impl.raiseJoinRoomPreEvent(player.getExternal(), source, isCancelled -> {
			if (!isCancelled)
				impl.addPending(player);
		});
	}

	@Override
	public boolean remove(String name) {

		// No player registered
		if (impl.getByName(name) == null)
			return false;

		VoxyPlayerImpl player = impl.getServer().getPlayerByName(name);

		// The player does not exist
		if (player == null)
			return false;

		impl.remove(player);
		return true;
	}

	@Override
	public void removeAll() {
		impl.removeAll();
	}

	@Override
	public Optional<IVoxyPlayer> get(String name) {
		VoxyPlayerImpl player = impl.getByName(name);
		return Optional.ofNullable(player == null ? null : player.getExternal());
	}

	@Override
	public int size() {
		return impl.size();
	}

	@Override
	public List<IVoxyPlayer> toList() {
		return impl.toList();
	}
}
