package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereEnableChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereRadiusChangedEvent;
import fr.pederobien.voxy.server.interfaces.ICoordinates;
import fr.pederobien.voxy.server.interfaces.ISoundProfile;
import fr.pederobien.voxy.server.interfaces.ISoundProfileArgs;
import fr.pederobien.voxy.server.interfaces.ISoundSphere;
import fr.pederobien.voxy.server.interfaces.ISoundVolumes;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class SoundSphere implements ISoundSphere {
	private final VoxyPlayerImpl center;
	private boolean isEnabled;
	private double xRadius;
	private double yRadius;
	private double zRadius;
	private ISoundProfile leftProfile;
	private ISoundProfile rightProfile;
	private ISoundProfile globalProfile;

	/**
	 * Creates a sound sphere attached to this player. The player's coordinates will be used as the sphere center.
	 * 
	 * @param player The player associated to this sphere.
	 */
	public SoundSphere(VoxyPlayerImpl player) {
		this.center = player;

		isEnabled = false;

		// Distance in meter.
		xRadius = 50;
		yRadius = 50;
		zRadius = 50;

		// By default, fixed sound volumes
		leftProfile = new DefaultLeftSoundProfile();
		rightProfile = new DefaultRightSoundProfile();
		globalProfile = new DefaultGlobalSoundProfile();
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		if (this.isEnabled == isEnabled)
			return;

		this.isEnabled = isEnabled;
		EventManager.callEvent(new VoxyPlayerSphereEnableChangedEvent(center.getExternal()));
	}

	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public double getXRadius() {
		return xRadius;
	}

	@Override
	public double getYRadius() {
		return yRadius;
	}

	@Override
	public double getZRadius() {
		return zRadius;
	}

	@Override
	public void setRadius(double xRadius, double yRadius, double zRadius) {
		double oldXRadius = this.xRadius;
		double oldyRadius = this.yRadius;
		double oldzRadius = this.zRadius;

		this.xRadius = xRadius < 0 ? 0 : xRadius;
		this.yRadius = yRadius < 0 ? 0 : yRadius;
		this.zRadius = zRadius < 0 ? 0 : zRadius;

		if (oldXRadius - xRadius != 0 || oldyRadius - yRadius != 0 || oldzRadius - zRadius != 0)
			EventManager.callEvent(new VoxyPlayerSphereRadiusChangedEvent(center.getExternal()));
	}

	@Override
	public void setLeftProfile(ISoundProfile leftProfile) {
		this.leftProfile = leftProfile;
	}

	@Override
	public void setRightProfile(ISoundProfile rightProfile) {
		this.rightProfile = rightProfile;
	}

	@Override
	public void setGlobalProfile(ISoundProfile globalProfile) {
		this.globalProfile = globalProfile;
	}

	@Override
	public ISoundVolumes computeVolumes(IVoxyPlayer player) {
		if (!isEnabled)
			return new SoundVolumes(1, 1, 1);

		LocalCoordinates local = toLocalCoordinates(player.getCoordinates());
		double distance = getDistance(local);

		ISoundProfileArgs args = new SoundProfileArgs(this, center.getName(), player.getName(), local, distance);
		float left = leftProfile.compute(args);
		float right = rightProfile.compute(args);
		float global = globalProfile.compute(args);

		return new SoundVolumes(left, right, global);
	}

	private LocalCoordinates toLocalCoordinates(ICoordinates other) {
		// Step 1: Getting the coordinates of the center
		ICoordinates center = getCenterCoordinates();

		// Step 1: Get relative position
		double x = other.getX() - center.getX();
		double y = other.getY() - center.getY();
		double z = other.getZ() - center.getZ();

		// Step 2: Apply transpose rotation matrix
		return new RotationMatrix(center.getYaw(), center.getPitch(), center.getRoll()).multiply(x, y, z);
	}

	/**
	 * Compute the distance between the point associated to the given position and the center of this sphere.
	 * 
	 * @param local The coordinates of a player relative to the center of this oriented ellipsoid.
	 * @return The distance of a player relative to the center of this ellipsoid.
	 */
	private double getDistance(LocalCoordinates local) {
		double x = (local.getX() * local.getX()) / (xRadius * xRadius);
		double y = (local.getY() * local.getY()) / (yRadius * yRadius);
		double z = (local.getZ() * local.getZ()) / (zRadius * zRadius);
		return x + y + z;
	}

	/**
	 * @return The coordinates of the center of this sound sphere.
	 */
	private ICoordinates getCenterCoordinates() {
		return center.getCoordinates();
	}

	private class SoundProfileArgs implements ISoundProfileArgs {
		private final ISoundSphere soundSphere;
		private final String listener;
		private final String speaker;
		private final LocalCoordinates local;
		private final double distance;

		public SoundProfileArgs(ISoundSphere soundSphere, String listener, String speaker, LocalCoordinates local, double distance) {
			this.soundSphere = soundSphere;
			this.listener = listener;
			this.speaker = speaker;
			this.local = local;
			this.distance = distance;
		}

		@Override
		public ISoundSphere getSoundSphere() {
			return soundSphere;
		}

		@Override
		public String getListener() {
			return listener;
		}

		@Override
		public String getSpeaker() {
			return speaker;
		}

		@Override
		public LocalCoordinates getLocalCoordinates() {
			return local;
		}

		@Override
		public double getDistance() {
			return distance;
		}
	}

	private class RotationMatrix {
		public final double m00, m01, m02;
		public final double m10, m11, m12;
		public final double m20, m21, m22;

		/**
		 * Creates the rotation matrix associated to the given yaw, pitch and roll angle.
		 * 
		 * @param yaw   The rotation around Z-Axis (looking left/right)
		 * @param pitch The rotation around X-Axis (looking up/down)
		 * @param roll  The rotation around Y-Axis (tilting head)
		 */
		public RotationMatrix(double yaw, double pitch, double roll) {
			double cy = MathHelper.getCosinus(-yaw), sy = MathHelper.getSinus(-yaw);
			double cp = MathHelper.getCosinus(-pitch), sp = MathHelper.getSinus(-pitch);
			double cr = MathHelper.getCosinus(-roll), sr = MathHelper.getSinus(-roll);

			m00 = cp * cy;
			m01 = cy * sp * sr - sy * cr;
			m02 = cy * sp * cr + sy * sr;

			m10 = sy * cp;
			m11 = sy * sp * sr + cy * cr;
			m12 = sy * sp * cr - cy * sr;

			m20 = -sp;
			m21 = cp * sr;
			m22 = cp * cr;
		}

		/**
		 * Multiply this matrix with the vector represented by the given 3 spatial distances .
		 * 
		 * @param x The x-distance to the center.
		 * @param y The y-distance to the center.
		 * @param z The z-distance to the center.
		 * @return The resulting vector whose coordinate are defined relative to the center of the ellipsoid.
		 */
		public LocalCoordinates multiply(double x, double y, double z) {
			double xLocal = m00 * x + m01 * y + m02 * z;
			double yLocal = m10 * x + m11 * y + m12 * z;
			double zLocal = m20 * x + m21 * y + m22 * z;
			return new LocalCoordinates(xLocal, yLocal, zLocal);
		}
	}
}
