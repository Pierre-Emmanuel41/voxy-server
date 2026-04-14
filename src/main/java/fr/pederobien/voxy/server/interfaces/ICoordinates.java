package fr.pederobien.voxy.server.interfaces;

public interface ICoordinates {

	/**
	 * @return The x value of the coordinate, in meter.
	 */
	double getX();

	/**
	 * @return The y value of the coordinate, in meter.
	 */
	double getY();

	/**
	 * @return The z value of the coordinate, in meter;
	 */
	double getZ();

	/**
	 * @return Rotation angle about the z-Axis, in radian.
	 */
	double getYaw();

	/**
	 * @return Second rotation angle about the x-Axis, in radian.
	 */
	double getPitch();

	/**
	 * @return Third rotation angle about the y-Axis, in radian.
	 */
	double getRoll();

	/**
	 * Update the x, y, z, yaw pitch and roll value of this coordinate.
	 * 
	 * @param x     The x value of the coordinate, in meter.
	 * @param y     The y value of the coordinate, in meter.
	 * @param z     The z value of the coordinate, in meter.
	 * @param yaw   The rotation angle about the z-Axis, in radian.
	 * @param pitch The rotation angle about the x-Axis, in radian.
	 * @param roll  The rotation angle about the y-Axis, in radian.
	 */
	void update(double x, double y, double z, double yaw, double pitch, double roll);
}
