package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSpeakingEvent extends VoxyPlayerEvent {
	private final float loudness;

	/**
	 * Creates an event thrown when a player is speaking.
	 * 
	 * @param player   The speaking player.
	 * @param loudness The energy of the sample.
	 */
	public VoxyPlayerSpeakingEvent(IVoxyPlayer player, float loudness) {
		super(player);

		this.loudness = loudness;
	}

	/**
	 * @return The energy of the sample.
	 */
	public float getLoudness() {
		return loudness;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("loudness=" + getLoudness());
		return String.format("%s_%s", getName(), joiner);
	}

}
