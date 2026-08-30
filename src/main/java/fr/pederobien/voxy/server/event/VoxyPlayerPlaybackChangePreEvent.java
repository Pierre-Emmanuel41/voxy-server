package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerPlaybackChangePreEvent extends VoxyPlayerEvent implements ICancellable {
	private final boolean playback;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when the play-back status of a player is about to change.
	 * 
	 * @param player   The player whose the play-back status is about to change
	 * @param playback True if the play-back will be enabled, false otherwise.
	 * @param source   The source that requires the play-back status to change.
	 */
	public VoxyPlayerPlaybackChangePreEvent(IVoxyPlayer player, boolean playback, ISource source) {
		super(player);

		this.playback = playback;
		this.source = source;
		isCancelled = false;
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
	 * @return True if the play-back will be enabled, false otherwise.
	 */
	public boolean isPlayback() {
		return playback;
	}

	/**
	 * @return The source that requires the play-back status to change.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer());
		joiner.add("playback=" + isPlayback());
		joiner.add("source=" + getSource());
		return String.format("%s_%s", getName(), joiner);
	}
}
