package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerPlaybackChangePostEvent extends VoxyPlayerEvent {
	private final boolean playback;

	/**
	 * Creates an event thrown when the play-back of a player has changed.
	 * 
	 * @param player   The player whose the play-back status has changed.
	 * @param playback True if the player shall hear its own audio stream, false otherwise.
	 */
	public VoxyPlayerPlaybackChangePostEvent(IVoxyPlayer player, boolean playback) {
		super(player);

		this.playback = playback;
	}

	/**
	 * @return True if the player shall hear its own audio stream, false otherwise.
	 */
	public boolean isPlayback() {
		return playback;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer());
		joiner.add("playback=" + isPlayback());
		return String.format("%s_%s", getName(), joiner);
	}
}
