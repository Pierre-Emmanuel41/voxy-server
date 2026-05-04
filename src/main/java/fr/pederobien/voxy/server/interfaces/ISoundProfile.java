package fr.pederobien.voxy.server.interfaces;

public interface ISoundProfile {

	/**
	 * Compute the volume value for a point located at x, y, z meter from the center of the sound sphere.
	 * 
	 * @param args The object that gather useful parameters to compute an audio volume.
	 */
	float compute(ISoundProfileArgs args);

	/**
	 * Compute the square root of a given number.
	 * 
	 * @param number The number.
	 * @return The square root of the given number.
	 */
	default float fastSqrt(double number) {
		// Bitwise approximation for speed
		double sqrt = Double.longBitsToDouble(((Double.doubleToLongBits(number) - (1l << 52)) >> 1) + (1l << 61));

		// One Newton-Raphson iteration to improve accuracy
		return (float) ((sqrt + number / sqrt) / 2.0);
	}
}
