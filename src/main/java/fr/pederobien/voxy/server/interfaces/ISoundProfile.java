package fr.pederobien.voxy.server.interfaces;

public interface ISoundProfile {

	/**
	 * Compute the volume value for a point located at x, y, z meter from the center of the sound sphere.
	 * 
	 * @param x       The x value of the point.
	 * @param xRadius The radius on x-Axis of the sound sphere.
	 * @param y       The y value of the point.
	 * @param yRadius The radius on y-Axis of the sound sphere.
	 * @param z       The z value of the point.
	 * @param zRadius The radius on z-Axis of the sound sphere.
	 * @return The computed volume, it shall be greater or equals to 0.
	 */
	float compute(double x, double xRadius, double y, double yRadius, double z, double zRadius);
}
