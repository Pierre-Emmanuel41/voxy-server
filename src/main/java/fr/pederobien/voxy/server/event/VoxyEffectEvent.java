package fr.pederobien.voxy.server.event;

import fr.pederobien.voxy.server.interfaces.IVoxyEffect;

public class VoxyEffectEvent extends VoxyEvent {
	private final IVoxyEffect effect;

	/**
	 * Creates an event associated to an effect.
	 * 
	 * @param effect The effect involved in this event.
	 */
	public VoxyEffectEvent(IVoxyEffect effect) {
		this.effect = effect;
	}

	/**
	 * @return The voxy effect involved in this event.
	 */
	public IVoxyEffect getEffect() {
		return effect;
	}
}
