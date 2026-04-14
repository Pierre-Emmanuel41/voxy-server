package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.ISoundVolumes;

public class SoundVolumes implements ISoundVolumes {
	private final float left;
	private final float right;
	private final float global;

	/**
	 * Creates a class that contains computed sound volumes.
	 * 
	 * @param left   The volumes for the left channel, minimum value is 0.
	 * @param right  The volumes for the right channel, minimum value is 0.
	 * @param global The volumes for both channels, minimum value is 0.
	 */
	public SoundVolumes(float left, float right, float global) {
		this.left = left < 0 ? 0 : left;
		this.right = right < 0 ? 0 : right;
		this.global = global < 0 ? 0 : global;
	}

	@Override
	public float getLeft() {
		return left;
	}

	@Override
	public float getRight() {
		return right;
	}

	@Override
	public float getGlobal() {
		return global;
	}
}
