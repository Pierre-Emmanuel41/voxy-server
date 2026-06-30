package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSphereEnableChangedEvent extends VoxyPlayerEvent {

	/**
	 * Creates an event thrown when the enable state of a player's sound sphere has changed.
	 * 
	 * @param player The player whose the sound sphere is enabled or disabled.
	 */
	public VoxyPlayerSphereEnableChangedEvent(IVoxyPlayer player) {
		super(player);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("isEnabled=" + getPlayer().getSoundSphere().isEnabled());
		return String.format("%s_%s", getName(), joiner);
	}
}
