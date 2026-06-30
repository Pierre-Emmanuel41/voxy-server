package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteStatusChangePreEvent extends VoxyPlayerEvent implements ICancellable {
	private final boolean isMute;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when the mute status of a player is about to change.
	 * 
	 * @param player The player whose the mute status is about to changed.
	 * @param isMute The new player's mute status.
	 * @param source The source that requires a player to change its mute status.
	 */
	public VoxyPlayerMuteStatusChangePreEvent(IVoxyPlayer player, boolean isMute, ISource source) {
		super(player);

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
	 * @return True if the player is about to mute itself, false if the player is about to unmute itself.
	 */
	public boolean isMute() {
		return isMute;
	}

	/**
	 * @return The source that requires a player to change its mute status.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("oldMute=" + getPlayer().isMute());
		joiner.add("newMute=" + isMute);
		joiner.add("source=" + getSource().getName());
		return joiner.toString();
	}
}
