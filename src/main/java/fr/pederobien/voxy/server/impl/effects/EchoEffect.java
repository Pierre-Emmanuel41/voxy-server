package fr.pederobien.voxy.server.impl.effects;

import fr.pederobien.voxy.server.interfaces.IEffect;

public class EchoEffect extends Effect implements IEffect {

	/**
	 * The name of this effect.
	 */
	public static final String NAME = fr.pederobien.voxy.common.impl.effects.EchoEffect.NAME;

	/**
	 * Creates an echo effect. The feedback and gain parameters modifies directly how the echo is done:<br>
	 * <br>
	 * Feedback:<br>
	 * 0.0: No repeats. You hear only the first echo (controlled by Gain), then silence.<br>
	 * 0.1 -> 0.4: A quick decay (2–3 repeats). Good for small room simulations.<br>
	 * 0.5 -> 0.7: A standard echo (4–8 repeats). The volume halves roughly every repeat.<br>
	 * 0.8 -> 0.9: A long, trailing echo (many repeats).<br>
	 * 1.0: Infinite sustain. The echo repeats forever at the same volume.<br>
	 * > 1.0: Runaway Feedback. The signal amplifies exponentially on every loop quickly hitting the maximum limit (Short.MAX_VALUE)
	 * and creating loud digital noise/static.<br>
	 * <br>
	 * gain:<br>
	 * 0.0: No echo is heard (the delay line still works, but the output is muted).<br>
	 * 0.1 -> 0.5: A subtle, background echo.<br>
	 * 0.6 -> 0.9: A prominent, distinct echo.<br>
	 * 1.0: The first echo is as loud as the original sound.<br>
	 * > 1.0: The first echo is louder than the original (can cause immediate clipping if the original signal is already loud).<br>
	 * <br>
	 * Note: The setParameters method expects a EchoEffect.Parameter argument.<br>
	 * 
	 * @param delay    The time, in ms, before repeating previous sample. It shall be in range [0, 2000].
	 * @param feedback Controls how much of the delayed signal is sent back into the delay line to create subsequent repetitions. It
	 *                 determines the number of repeats and the decay rate.
	 * @param gain     Controls the volume of the first echo repetition relative to the original (dry) sound. It determines how loud
	 *                 the echo is when it first becomes audible.
	 */
	public EchoEffect(int delay, float feedback, float gain) {
		super(NAME);
		add(fr.pederobien.voxy.common.impl.effects.EchoEffect.DELAY, delay);
		add(fr.pederobien.voxy.common.impl.effects.EchoEffect.FEEDBACK, feedback);
		add(fr.pederobien.voxy.common.impl.effects.EchoEffect.GAIN, gain);
	}
}
