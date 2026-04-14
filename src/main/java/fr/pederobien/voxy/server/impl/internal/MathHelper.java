package fr.pederobien.voxy.server.impl.internal;

public class MathHelper {
	private static final double ROUNDED_PI = Math.round(Math.PI * 1000.0) / 1000.0;
	private static final double ROUNDED_PI_OUT_OF_2 = Math.round(Math.PI / 2 * 1000.0) / 1000.0;
	private static final double ROUNDED_2PI = Math.round(2 * Math.PI * 1000.0) / 1000.0;

	private static final double PRECISION = 0.0001;
	private static final int STEPS = (int) Math.round((Math.PI / 2.0) / PRECISION) + 1;
	private static final double[] COS_VALUES = new double[STEPS + 1];
	private static final double[] SIN_VALUES = new double[STEPS + 1];

	static {
		for (int i = 0; i < STEPS; i++) {
			double angle = i * PRECISION;
			COS_VALUES[i] = Math.round(Math.cos(angle) * 1000.0) / 1000.0;
			SIN_VALUES[i] = Math.round(Math.sin(angle) * 1000.0) / 1000.0;

			if (i == STEPS - 1) {
				COS_VALUES[i + 1] = 0.0;
				SIN_VALUES[i + 1] = 1.0;
			}
		}
	}

	/**
	 * Get the cosinus of the given angle.
	 * 
	 * @param angle The angle in radian.
	 * @return The cosinus of the angle.
	 */
	public static double getCosinus(double angle) {
		double rounded = Math.round(inRange(angle) * 1000.0) / 1000.0;

		// Case 1: angle in range [0, PI/2]
		if (0 <= rounded && rounded <= ROUNDED_PI_OUT_OF_2)
			return getValue(rounded, COS_VALUES);

		// Case 2: angle in range [0, -PI/2]
		if (-ROUNDED_PI_OUT_OF_2 <= rounded && rounded <= 0)
			return getValue(-rounded, COS_VALUES);

		// Case 3: angle in range [-PI, -PI/2[
		if (-ROUNDED_PI <= rounded && rounded < -ROUNDED_PI_OUT_OF_2)
			return -getValue(rounded + ROUNDED_PI, COS_VALUES);

		// Case 4: angle in range [PI/2, PI]
		return -getValue(ROUNDED_PI - rounded, COS_VALUES);
	}

	/**
	 * Get the sinus of the given angle.
	 * 
	 * @param angle The angle in radian.
	 * @return The sinus of the angle.
	 */
	public static double getSinus(double angle) {
		double rounded = Math.round(inRange(angle) * 1000.0) / 1000.0;

		// Case 1: angle in range [0, PI/2]
		if (0 <= rounded && rounded <= ROUNDED_PI_OUT_OF_2)
			return getValue(rounded, SIN_VALUES);

		// Case 2: angle in range [0, -PI/2]
		if (-ROUNDED_PI_OUT_OF_2 <= rounded && rounded <= 0)
			return -getValue(-rounded, SIN_VALUES);

		// Case 3: angle in range [-PI, -PI/2[
		if (-ROUNDED_PI <= rounded && rounded < -ROUNDED_PI_OUT_OF_2)
			return -getValue(rounded + ROUNDED_PI, SIN_VALUES);

		// Case 4: angle in range [PI/2, PI]
		return getValue(ROUNDED_PI - rounded, SIN_VALUES);
	}

	/**
	 * Put back the given angle in range [-Pi, Pi].
	 * 
	 * @param angle The angle to check.
	 * @return The angle in range [-Pi, Pi].
	 */
	private static double inRange(double angle) {
		if (angle > Math.PI)
			return angle - ROUNDED_2PI;
		if (angle < -Math.PI)
			return angle + ROUNDED_2PI;

		return angle;
	}

	/**
	 * Get the function value associated to the given angle.
	 * 
	 * @param angle    The angle to use to get the associated cosinus or sinus.
	 * @param function The array that contains functions values.
	 * @return The value associated to the given angle.
	 */
	private static double getValue(double angle, double[] function) {
		int index = (int) Math.round(angle / PRECISION);
		return function[index < function.length ? index : function.length - 1];
	}
}
