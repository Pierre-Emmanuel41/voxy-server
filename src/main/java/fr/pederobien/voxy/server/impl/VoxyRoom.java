package fr.pederobien.voxy.server.impl;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.event.RenameRoomPrevent;
import fr.pederobien.voxy.server.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyRoom implements IVoxyRoom, IEventListener {
	private final VoxyRoomImpl roomImpl;

	/**
	 * Creates a room where players can speak together.
	 *
	 * @param roomImpl The room implementation.
	 */
	public VoxyRoom(VoxyRoomImpl roomImpl) {
		this.roomImpl = roomImpl;
	}

	@Override
	public IVoxyServer getServer() {
		return roomImpl.getServer().getExternal();
	}

	@Override
	public String getName() {
		return roomImpl.getName();
	}

	@Override
	public boolean setName(String name) {
		RenameRoomPrevent preEvent = new RenameRoomPrevent(this, name);
		EventManager.callEvent(preEvent, () -> roomImpl.setName(name));

		// Event not cancelled to the room has been renamed.
		return !preEvent.isCancelled();
	}

	@Override
	public IPlayerList getPlayers() {
		return roomImpl.getPlayers().getExternal();
	}

	@Override
	public String toString() {
		return roomImpl.toString();
	}
}
