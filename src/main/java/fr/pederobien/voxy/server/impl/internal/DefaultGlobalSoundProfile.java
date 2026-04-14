package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.ISoundProfile;

public class DefaultGlobalSoundProfile implements ISoundProfile {

	@Override
	public float compute(double x, double xRadius, double y, double yRadius, double z, double zRadius) {
		return (float) (1 - ((x * x) / (xRadius * xRadius) + (y * y) / (yRadius * yRadius) + (z * z) / (zRadius * zRadius)));
	}
}
