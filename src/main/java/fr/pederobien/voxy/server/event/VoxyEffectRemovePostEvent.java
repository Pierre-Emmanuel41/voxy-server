package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyEffectRemovePostEvent extends VoxyEffectEvent {
	private final String effectName;
	private final IVoxyPlayer listening;
	private final IVoxyPlayer speaking;

	/**
	 * Creates an event thrown when an effect is removed from the audio stream of a player. The effect is null, to access the effect's
	 * name, call getEffectName().
	 * 
	 * @param effectName The name of the removed effect.
	 * @param speaking   The speaking player
	 * @param listening  The listening player.
	 */
	public VoxyEffectRemovePostEvent(String effectName, IVoxyPlayer speaking, IVoxyPlayer listening) {
		super(null);

		this.effectName = effectName;
		this.speaking = speaking;
		this.listening = listening;
	}

	/**
	 * @return The name of the removed effect.
	 */
	public String getEffectName() {
		return effectName;
	}

	/**
	 * @return The listening player.
	 */
	public IVoxyPlayer getListening() {
		return listening;
	}

	/**
	 * @return The speaking player.
	 */
	public IVoxyPlayer getSpeaking() {
		return speaking;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("effect=" + getEffectName());
		joiner.add("speaking=" + getSpeaking().getName());
		joiner.add("listening=" + getListening().getName());

		return String.format("%s_%s", getName(), joiner);
	}
}
