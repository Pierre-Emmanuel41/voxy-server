package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerSphereRadiusChangedEvent extends VoxyPlayerEvent {

	/**
	 * Creates an event thrown when the radius of the player's sound sphere has changed.
	 * 
	 * @param player The player whose the sound sphere radius has changed.
	 */
	public VoxyPlayerSphereRadiusChangedEvent(IVoxyPlayer player) {
		super(player);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("xRadius=" + getPlayer().getSoundSphere().getXRadius());
		joiner.add("yRadius=" + getPlayer().getSoundSphere().getYRadius());
		joiner.add("zRadius=" + getPlayer().getSoundSphere().getZRadius());

		return String.format("%s_%s", getName(), joiner);
	}
}
