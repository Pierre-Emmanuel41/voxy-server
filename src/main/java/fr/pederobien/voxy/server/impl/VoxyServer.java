package fr.pederobien.voxy.server.impl;

import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.impl.internal.VoxyServerImpl;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyServer implements IVoxyServer, IEventListener {
	private final VoxyServerImpl serverImpl;

	/**
	 * Creates a voxy server.
	 *
	 * @param serverImpl The implementation of this server.
	 */
	public VoxyServer(VoxyServerImpl serverImpl) {
		this.serverImpl = serverImpl;
	}

	@Override
	public String getName() {
		return serverImpl.getName();
	}

	@Override
	public void open() {
		serverImpl.open();
	}

	@Override
	public void close() {
		serverImpl.close();
	}

	@Override
	public void dispose() {
		serverImpl.dispose();
	}

	@Override
	public IRoomList getRooms() {
		return serverImpl.getRooms().getExternal();
	}

	@Override
	public String toString() {
		return serverImpl.toString();
	}
}
