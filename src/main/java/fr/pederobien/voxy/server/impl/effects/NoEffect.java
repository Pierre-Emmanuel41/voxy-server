package fr.pederobien.voxy.server.impl.effects;

import fr.pederobien.voxy.server.interfaces.IEffect;

public class NoEffect extends Effect implements IEffect {

	/**
	 * The name of this effect.
	 */
	public static final String NAME = fr.pederobien.voxy.common.impl.effects.NoEffect.NAME;

	/**
	 * Creates a holder for a NoEffect.
	 */
	public NoEffect() {
		super(NAME);
	}

}
