package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.ISoundProfile;

public class DefaultLeftSoundProfile implements ISoundProfile {

	@Override
	public float compute(double x, double xRadius, double y, double yRadius, double z, double zRadius) {
		return (float) (1.0 + y / yRadius);
	}

}
