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
	private float left;
	private float right;
	private float global;

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
		left = 1;
		right = 1;
		global = 1;
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

	/**
	 * @return The volume on the left side.
	 */
	public float getLeft() {
		return left;
	}

	/**
	 * Set the volume on the left side.
	 * 
	 * @param left The volume on the left side.
	 */
	public void setLeft(float left) {
		this.left = left;
	}

	/**
	 * @return The volume on the right side.
	 */
	public float getRight() {
		return right;
	}

	/**
	 * Set the volume on the right side.
	 * 
	 * @param right The volume on the right side.
	 */
	public void setRight(float right) {
		this.right = right;
	}

	/**
	 * @return The global volume on both sides.
	 */
	public float getGlobal() {
		return global;
	}

	/**
	 * Set the global volume on both sides.
	 * 
	 * @param global The global volume on both sides.
	 */
	public void setGlobal(float global) {
		this.global = global;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("sampleSize=" + getSample().length);
		joiner.add("algorithm=" + getAlgorithm());
		joiner.add("left=" + getLeft());
		joiner.add("right=" + getRight());
		joiner.add("global=" + getGlobal());

		StringJoiner playersJoiner = new StringJoiner(",", "{", "}");
		for (IVoxyPlayer player : players)
			playersJoiner.add(player.getName());

		joiner.add("receivers=" + playersJoiner);
		return String.format("%s_%s", getName(), joiner);
	}
}
