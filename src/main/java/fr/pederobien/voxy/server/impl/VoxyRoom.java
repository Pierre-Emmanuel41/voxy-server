package fr.pederobien.voxy.server.impl;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.event.RenameRoomPrevent;
import fr.pederobien.voxy.server.impl.internal.VoxyRoomImpl;
import fr.pederobien.voxy.server.interfaces.IPlayerList;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyRoom implements IVoxyRoom, IEventListener {
	private final VoxyRoomImpl impl;

	/**
	 * Creates a room where players can speak together.
	 *
	 * @param impl The room implementation.
	 */
	public VoxyRoom(VoxyRoomImpl impl) {
		this.impl = impl;
	}

	@Override
	public IVoxyServer getServer() {
		return impl.getServer().getExternal();
	}

	@Override
	public String getName() {
		return impl.getName();
	}

	@Override
	public boolean setName(String name) {
		RenameRoomPrevent preEvent = new RenameRoomPrevent(this, name);
		EventManager.callEvent(preEvent, () -> impl.setName(name));

		// Event not cancelled to the room has been renamed.
		return !preEvent.isCancelled();
	}

	@Override
	public IPlayerList getPlayers() {
		return impl.getPlayers().getExternal();
	}

	@Override
	public void setPlayBack(boolean playBack) {
		impl.setPlayBack(playBack);
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
