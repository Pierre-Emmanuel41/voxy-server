package fr.pederobien.voxy.server.impl;

import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.interfaces.ICoordinates;
import fr.pederobien.voxy.server.interfaces.IEffect;
import fr.pederobien.voxy.server.interfaces.ISoundSphere;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;
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
	public IVoxyRoom getRoom() {
		return impl.getRoom().getExternal();
	}

	@Override
	public boolean isMute() {
		return impl.isMute();
	}

	@Override
	public boolean setMute(boolean isMute, ISource source) {
		if (isMute() == isMute)
			return false;

		// Event not cancelled so the mute status has been updated
		return !impl.raisePlayerMuteStatusChangePreEvent(isMute, source, isCancelled -> {
			if (!isCancelled)
				impl.setMute(isMute);
		});
	}

	@Override
	public boolean setMuteBy(boolean isMute, IVoxyPlayer player, ISource source) {
		VoxyPlayerImpl playerImpl = impl.getServer().getPlayerByName(player.getName());
		if (playerImpl == null)
			return false;

		// Event not cancelled so the mute status for the given player has been updated
		return !impl.raisePlayerMuteByStatusChangePreEvent(player, isMute, source, isCancelled -> {
			if (!isCancelled) {
				impl.setMuteBy(playerImpl, isMute);
			}
		});
	}

	@Override
	public boolean setPlayback(boolean playback, ISource source) {
		return !impl.raisePlayerPlaybackChangePreEvent(playback, source, isCancelled -> {
			if (!isCancelled)
				impl.setPlayback(playback);
		});
	}

	@Override
	public boolean isPlayback() {
		return impl.isPlayback();
	}

	@Override
	public void addEffect(IVoxyPlayer speaker, int index, IEffect holder) {
		impl.addEffect(speaker, index, holder);
	}

	@Override
	public void removeEffect(IVoxyPlayer speaker, String effectName) {
		impl.removeEffect(speaker, effectName);
	}

	@Override
	public void updateEffect(IVoxyPlayer speaker, IEffect holder) {
		impl.updateEffect(speaker, holder);
	}

	@Override
	public boolean isDeaf() {
		return impl.isDeaf();
	}

	@Override
	public ICoordinates getCoordinates() {
		return impl.getCoordinates();
	}

	@Override
	public ISoundSphere getSoundSphere() {
		return impl.getSoundSphere();
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
