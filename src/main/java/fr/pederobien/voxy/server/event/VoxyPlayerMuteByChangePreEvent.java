package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteByChangePreEvent extends VoxyPlayerEvent implements ICancellable {
	private final IVoxyPlayer target;
	private final boolean isMute;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a player is about to mute another player for himself.
	 * 
	 * @param player The player that mutes/unmutes another player.
	 * @param target The player to mute/unmute for another player.
	 * @param isMute True to mute, false to unmute.
	 * @param source The source that requires a player to be muted for another player.
	 */
	public VoxyPlayerMuteByChangePreEvent(IVoxyPlayer player, IVoxyPlayer target, boolean isMute, ISource source) {
		super(player);

		this.target = target;
		this.isMute = isMute;
		this.source = source;
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

	/**
	 * @return The source that requires a player to be muted for another player.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("target=" + getTarget().getName());
		joiner.add("isMute=" + isMute());
		joiner.add("source=" + getSource().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
