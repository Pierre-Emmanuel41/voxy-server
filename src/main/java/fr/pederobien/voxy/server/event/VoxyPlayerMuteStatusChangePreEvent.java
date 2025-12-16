package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteStatusChangePreEvent extends VoxyPlayerEvent implements ICancellable {
	private boolean isMute;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when the mute status of a player is about to change.
	 * 
	 * @param player The player whose the mute status is about to changed.
	 * @param isMute The new player's mute status.
	 */
	public VoxyPlayerMuteStatusChangePreEvent(IVoxyPlayer player, boolean isMute) {
		super(player);

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
	 * @return True if the player is about to mute itself, false if the player is
	 *         about to unmute itself.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("oldMute=" + getPlayer().isMute());
		joiner.add("newMute=" + isMute);
		return joiner.toString();
	}
}
