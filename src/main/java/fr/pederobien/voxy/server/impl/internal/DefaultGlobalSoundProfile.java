package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.ISoundProfile;
import fr.pederobien.voxy.server.interfaces.ISoundProfileArgs;

public class DefaultGlobalSoundProfile implements ISoundProfile {

	@Override
	public float compute(ISoundProfileArgs args) {
		if (args.getDistance() > 1)
			return 0f;

		return fastSqrt(1 - args.getDistance());
	}
}
