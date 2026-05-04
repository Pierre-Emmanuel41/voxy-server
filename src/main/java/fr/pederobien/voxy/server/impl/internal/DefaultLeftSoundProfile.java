package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.voxy.server.interfaces.ISoundProfile;
import fr.pederobien.voxy.server.interfaces.ISoundProfileArgs;

public class DefaultLeftSoundProfile implements ISoundProfile {

	@Override
	public float compute(ISoundProfileArgs args) {
		// Let's write A the point associated to the local coordinates: A(Xa, Ya, Za).
		// The left volume is independent of Za.
		// Let's write alpha the angle between the point and the x-Axis
		// sin(alpha) = Ya / sqrt(Xa^2 + Ya^2)
		// When alpha = 0, the left volume is 1, when alpha = 90°, the left volume is 2
		// left = 1 + sin(alpha) = 1 + Ya / sqrt(Xa^2 + Ya^2)

		double x = args.getLocalCoordinates().getX();
		double y = args.getLocalCoordinates().getY();

		if (x == 0 && y == 0)
			return 1.0f;

		return (float) (1 + y / fastSqrt(x * x + y * y));
	}
}
