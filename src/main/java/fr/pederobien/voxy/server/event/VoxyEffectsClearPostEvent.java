package fr.pederobien.voxy.server.event;

import java.util.List;
import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyEffect;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyEffectsClearPostEvent extends VoxyEffectEvent {
	private final List<IVoxyEffect> effects;
	private final IVoxyPlayer speaking;
	private final IVoxyPlayer listening;

	/**
	 * Creates an event thrown when all effects shall be cleared between two players.
	 * 
	 * @param speaking  The speaking player.
	 * @param listening The listening player.
	 */
	public VoxyEffectsClearPostEvent(List<IVoxyEffect> effects, IVoxyPlayer speaking, IVoxyPlayer listening) {
		super(null);

		this.effects = effects;
		this.speaking = speaking;
		this.listening = listening;
	}

	/**
	 * @return The list of effects that shall be removed.
	 */
	public List<IVoxyEffect> getEffects() {
		return effects;
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
		joiner.add("speaking=" + getSpeaking().getName());
		joiner.add("listening=" + getListening().getName());

		return String.format("%s_%s", getName(), joiner);
	}
}
