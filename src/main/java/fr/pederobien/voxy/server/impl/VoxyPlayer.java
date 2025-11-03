package fr.pederobien.voxy.server.impl;

import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyPlayer implements IVoxyPlayer {
	private final VoxyPlayerImpl impl;

	/***
	 * Creates a player to move from one room to another.
	 *
	 * @param impl The player's implementation.
	 */
	public VoxyPlayer(VoxyPlayerImpl impl) {
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
	public boolean isMute() {
		return impl.isMute();
	}

	@Override
	public void setMute(boolean isMute) {
		impl.setMute(isMute);
	}

	@Override
	public boolean isDeaf() {
		return impl.isDeaf();
	}

	@Override
	public String toString() {
		return impl.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VoxyPlayer))
			return false;

		VoxyPlayer other = (VoxyPlayer) obj;
		return impl.equals(other.impl);
	}
}
