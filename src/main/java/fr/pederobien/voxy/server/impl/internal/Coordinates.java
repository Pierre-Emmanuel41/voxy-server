package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerCoordinateChangedEvent;
import fr.pederobien.voxy.server.interfaces.ICoordinates;

public class Coordinates implements ICoordinates {
	/**
	 * 1cm, minimum gap to detect a change and throw an event.
	 */
	private static final double DISTANCE_GAP = 0.01;

	/**
	 * 5°, minimum rotation angle to detect a change and throw an event.
	 */
	private static final double ANGLE_GAP = 0.08;

	private final VoxyPlayerImpl player;
	private double x;
	private double y;
	private double z;
	private double yaw;
	private double pitch;
	private double roll;

	/**
	 * Creates a coordinate associated to a player.
	 * 
	 * @param player The player associated to this coordinate.
	 */
	public Coordinates(VoxyPlayerImpl player) {
		this.player = player;

		// Center of the map
		x = 0;
		y = 0;
		z = 0;

		// Looks in the direction of x positive
		yaw = 0;
		pitch = 0;
		roll = 0;
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getZ() {
		return z;
	}

	@Override
	public double getYaw() {
		return yaw;
	}

	@Override
	public double getPitch() {
		return pitch;
	}

	@Override
	public double getRoll() {
		return roll;
	}

	@Override
	public void update(double x, double y, double z, double yaw, double pitch, double roll) {
		double previousX = this.x;
		double previousY = this.y;
		double previousZ = this.z;
		double previousYaw = this.yaw;
		double previousPitch = this.pitch;
		double previousRoll = this.roll;

		if (Math.abs(x - previousX) > DISTANCE_GAP)
			this.x = x;

		if (Math.abs(y - previousY) > DISTANCE_GAP)
			this.y = y;

		if (Math.abs(z - previousZ) > DISTANCE_GAP)
			this.z = z;

		if (Math.abs(yaw - previousYaw) > ANGLE_GAP)
			this.yaw = yaw;

		if (Math.abs(pitch - previousPitch) > ANGLE_GAP)
			this.pitch = pitch;

		if (Math.abs(roll - previousRoll) > ANGLE_GAP)
			this.roll = roll;

		boolean position = previousX != x || previousY != y || previousZ != z;
		boolean head = previousYaw != yaw || previousPitch != pitch || previousRoll != roll;

		// In case of at one coordinate has changed
		if (position || head)
			EventManager.callEvent(new VoxyPlayerCoordinateChangedEvent(player.getExternal(), position, head));
	}
}
