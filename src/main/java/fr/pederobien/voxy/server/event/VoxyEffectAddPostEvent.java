package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyEffect;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyEffectAddPostEvent extends VoxyEffectEvent {
	private final IVoxyPlayer speaking;
	private final IVoxyPlayer listening;

	/**
	 * Creates an event thrown when an effect is added.
	 * 
	 * @param effect    The added effect.
	 * @param speaking  The speaking player.
	 * @param listening The listening player.
	 * @param index     The index at which the effect shall be added.
	 */
	public VoxyEffectAddPostEvent(IVoxyEffect effect, IVoxyPlayer speaking, IVoxyPlayer listening) {
		super(effect);

		this.speaking = speaking;
		this.listening = listening;
	}

	/**
	 * @return The speaking player.
	 */
	public IVoxyPlayer getSpeaking() {
		return speaking;
	}

	/**
	 * @return The listening player.
	 */
	public IVoxyPlayer getListening() {
		return listening;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("effect=" + getEffect().getName());
		joiner.add("speaking=" + getSpeaking().getName());
		joiner.add("listening=" + getListening().getName());

		return String.format("%s_%s", getName(), joiner);
	}
}
