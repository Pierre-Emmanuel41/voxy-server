package fr.pederobien.voxy.server.impl;

import java.util.List;
import java.util.Optional;

import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.impl.internal.VoxyPlayerImpl;
import fr.pederobien.voxy.server.impl.internal.VoxyServerImpl;
import fr.pederobien.voxy.server.interfaces.IEffectManager;
import fr.pederobien.voxy.server.interfaces.IRoomList;
import fr.pederobien.voxy.server.interfaces.IVoxyEffect;
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
	public int getPort() {
		return impl.getPort();
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
	public IEffectManager getEffectManager() {
		return impl.getEffectManager();
	}

	@Override
	public void addOrUpdateEffect(IVoxyPlayer listening, IVoxyPlayer speaking, IVoxyEffect effect) {
		impl.addOrUpdateEffect(listening, speaking, effect);
	}

	@Override
	public void removeEffect(IVoxyPlayer listening, IVoxyPlayer speaking, String effectName) {
		impl.removeEffect(listening, speaking, effectName);
	}

	@Override
	public void clearEffects(IVoxyPlayer listening, IVoxyPlayer speaking) {
		impl.clearEffects(listening, speaking);
	}

	@Override
	public void addOrUpdateEffectForEachSpeakingPlayer(IVoxyPlayer listening, IVoxyEffect effect) {
		impl.addOrUpdateEffectForEachSpeakingPlayer(listening, effect);
	}

	@Override
	public void addOrUpdateEffectForEachListeningPlayer(IVoxyPlayer speaking, IVoxyEffect effect) {
		impl.addOrUpdateEffectForEachListeningPlayer(speaking, effect);
	}

	@Override
	public void removeEffectForEachSpeakingPlayer(IVoxyPlayer listening, String effectName) {
		impl.removeEffectForEachSpeakingPlayer(listening, effectName);
	}

	@Override
	public void removeEffectForEachListeningPlayer(IVoxyPlayer speaking, String effectName) {
		impl.removeEffectForEachListeningPlayer(speaking, effectName);
	}

	@Override
	public String toString() {
		return impl.toString();
	}
}
