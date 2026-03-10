package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyServerImpl;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServer implements IVoxyServer, IEventListener {
	private final VoxyServerImpl impl;

	/**
	 * Creates a voxy server.
	 *
	 * @param impl The implementation of this server.
	 */
	public VoxyServer(VoxyServerImpl impl) {
		this.impl = impl;
	}

	@Override
	public String getName() {
		return impl.getName();
	}

	@Override
	public boolean open() {
		return impl.open();
	}

	@Override
	public boolean close() {
		return impl.close();
	}

	@Override
	public boolean dispose() {
		return impl.dispose();
	}

	@Override
	public boolean isOpened() {
		return impl.isOpened();
	}

	@Override
	public boolean isDisposed() {
		return impl.isDisposed();
	}

	@Override
	public IRoomList getRooms() {
		return impl.getRooms().getExternal();
	}

	@Override
	public List<IVoxyPlayer> getPlayers() {
		return impl.getPlayers();
	}

	@Override
	public Optional<IVoxyPlayer> getPlayerByName(String name) {
		VoxyPlayerImpl player = impl.getPlayerByName(name);
		return Optional.ofNullable(player == null ? null : player.getExternal());
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
