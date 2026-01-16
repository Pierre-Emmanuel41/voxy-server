package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteByChangePreEvent extends VoxyPlayerEvent implements ICancellable {
	private final IVoxyPlayer target;
	private final boolean isMute;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a player is about to mute another player for himself.
	 * 
	 * @param source The player that mutes/unmutes another player.
	 * @param target The player to mute/unmute for another player.
	 * @param isMute True to mute, false to unmute.
	 */
	public VoxyPlayerMuteByChangePreEvent(IVoxyPlayer source, IVoxyPlayer target, boolean isMute) {
		super(source);

		this.target = target;
		this.isMute = isMute;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The player to mute/unmute.
	 */
	public IVoxyPlayer getTarget() {
		return target;
	}

	/**
	 * @return True if the target player is about to be mute, false if it is about to be unmute.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("source=" + getPlayer().getName());
		joiner.add("target=" + getTarget().getName());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
