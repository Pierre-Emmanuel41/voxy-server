package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSpeakingEvent extends VoxyPlayerEvent {

	/**
	 * Creates an event thrown when a player is speaking.
	 * 
	 * @param player The speaking player.
	 */
	public VoxyPlayerSpeakingEvent(IVoxyPlayer player) {
		super(player);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}

}
