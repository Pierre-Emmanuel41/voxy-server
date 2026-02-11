package fr.pederobien.voxy.server.event;

import java.util.List;
import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSpeakingPostEvent extends VoxyPlayerEvent {
	private final List<IVoxyPlayer> receivers;
	private final byte[] sample;
	private final byte algorithm;

	/**
	 * Creates an event thrown when the audio sample of a player shall be dispatch to other players.
	 * 
	 * @param event The pre-event that contains parameters to use by receivers's client.
	 */
	public VoxyPlayerSpeakingPostEvent(VoxyPlayerSpeakingPreEvent event) {
		super(event.getPlayer());

		receivers = event.getPlayers();
		sample = event.getSample();
		algorithm = event.getAlgorithm();
	}

	/**
	 * @return The list of players that shall receive the audio sample.
	 */
	public List<IVoxyPlayer> getReceivers() {
		return receivers;
	}

	/**
	 * @return The bytes array that contains the audio sample.
	 */
	public byte[] getSample() {
		return sample;
	}

	/**
	 * @return The algorithm used to compress the audio sample.
	 */
	public byte getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("sampleSize=" + getSample().length);
		joiner.add("algorithm=" + getAlgorithm());

		StringJoiner playersJoiner = new StringJoiner(",", "{", "}");
		for (IVoxyPlayer player : receivers)
			playersJoiner.add(player.getName());

		joiner.add("receivers=" + playersJoiner);
		return String.format("%s_%s", getName(), joiner);
	}
}
