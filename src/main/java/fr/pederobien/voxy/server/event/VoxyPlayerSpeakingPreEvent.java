package fr.pederobien.voxy.server.event;

import java.util.List;
import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSpeakingPreEvent extends VoxyPlayerEvent implements ICancellable {
	private final List<IVoxyPlayer> players;
	private final byte[] sample;
	private final byte algorithm;
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a player is speaking in a voxy room.
	 * 
	 * @param player    The speaking player.
	 * @param players   The list of players registered in the same room as the speaking player.
	 * @param sample    The bytes array that contains the audio sample.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public VoxyPlayerSpeakingPreEvent(IVoxyPlayer player, List<IVoxyPlayer> players, byte[] sample, byte algorithm) {
		super(player);

		this.players = players;
		this.sample = sample;
		this.algorithm = algorithm;
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
	 * @return The list of players registered in the same room as the speaking player.
	 */
	public List<IVoxyPlayer> getPlayers() {
		return players;
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
		for (IVoxyPlayer player : players)
			playersJoiner.add(player.getName());

		joiner.add("receivers=" + playersJoiner);
		return String.format("%s_%s", getName(), joiner);
	}
}
